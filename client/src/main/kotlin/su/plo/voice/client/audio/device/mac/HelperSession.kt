package su.plo.voice.client.audio.device.mac

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.util.AudioUtil
import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameType
import su.plo.voice.mac.protocol.frame.FrameWriter
import su.plo.voice.mac.protocol.message.Downstream
import su.plo.voice.mac.protocol.message.Upstream
import su.plo.voice.mac.protocol.message.status.AuthStatus
import su.plo.voice.mac.protocol.message.wire.PROTOCOL_VERSION
import su.plo.voice.mac.protocol.message.wire.toDownstream
import su.plo.voice.mac.protocol.message.wire.toFrame
import java.io.Closeable
import java.io.IOException
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

private const val REPLY_TIMEOUT_MS = 5_000L
private const val PROMPT_TIMEOUT_MS = 120_000L
private const val PING_INTERVAL_MS = 5_000L
private const val WEDGE_TIMEOUT_MS = 15_000L

/**
 * A live connection to one helper process.
 *
 * Audio arrives on a reader thread and is handed to [onAudio]. Everything else is just
 * request / response.
 */
internal class HelperSession(private val socket: Socket, token: String) : Closeable {
    private val frames = FrameReader(socket.getInputStream().asSource().buffered())
    private val writer = FrameWriter(socket.getOutputStream().asSink().buffered())
    private val pending = ArrayDeque<Pending>()
    private val writeLock = Any()
    private val nextRequestId = AtomicInteger()

    private class Pending(val requestId: Int, val future: CompletableFuture<Downstream>)

    @Volatile
    var onAudio: (ShortArray) -> Unit = {}

    @Volatile
    var onDevices: (Downstream.Devices) -> Unit = {}

    @Volatile
    var alive = true; private set

    @Volatile
    private var lastFrameAt = System.currentTimeMillis()

    val pid: Int

    init {
        val hello = frames.read()?.toDownstream() as? Downstream.Hello ?: throw IOException("The helper did not say hello")
        if (hello.token != token) throw IOException("The helper's token does not match")
        if (hello.version != PROTOCOL_VERSION) {
            throw IOException("The helper speaks protocol ${hello.version}, expected $PROTOCOL_VERSION")
        }

        pid = hello.pid
        thread(name = "Plasmo Voice Microphone", isDaemon = true, block = ::pump)
        thread(name = "Plasmo Voice Microphone Watchdog", isDaemon = true, block = ::watchdog)
    }

    fun openMicrophone(format: CaptureFormat, deviceId: String?): Int =
        expect<Downstream.Opened>(request(REPLY_TIMEOUT_MS) { Upstream.Open(format, deviceId, it) }).frameSamples

    fun closeMicrophone() {
        expect<Downstream.Closed>(request(REPLY_TIMEOUT_MS) { Upstream.Close(it) })
    }

    fun listDevices(): Downstream.Devices =
        expect(request(REPLY_TIMEOUT_MS) { Upstream.ListDevices(it) })

    fun permission(prompt: Boolean): AuthStatus =
        expect<Downstream.Permission>(
            request(if (prompt) PROMPT_TIMEOUT_MS else REPLY_TIMEOUT_MS) { Upstream.Permission(prompt, it) }
        ).status

    fun openSettings() = send(Upstream.OpenSettings.toFrame())

    override fun close() {
        alive = false
        runCatching { socket.close() }
    }

    private fun pump() {
        try {
            while (true) {
                val frame = frames.read() ?: break
                lastFrameAt = System.currentTimeMillis()

                when (frame.type) {
                    FrameType.AUDIO -> onAudio(AudioUtil.bytesToShorts(frame.payload))
                    FrameType.CONTROL -> deliver(frame.toDownstream())
                    FrameType.PING -> Unit
                }
            }
        } catch (_: Exception) {
        } finally {
            alive = false
            drainPending().forEach { it.future.completeExceptionally(IOException("The helper is gone")) }
            runCatching { socket.close() }
        }
    }

    private fun watchdog() {
        while (alive) {
            Thread.sleep(PING_INTERVAL_MS)

            val idleFor = System.currentTimeMillis() - lastFrameAt
            when {
                idleFor >= WEDGE_TIMEOUT_MS -> close()
                idleFor >= PING_INTERVAL_MS -> runCatching { send(Frame(FrameType.PING, ByteArray(0))) }
            }
        }
    }

    private fun deliver(message: Downstream) {
        val requestId = when (message) {
            is Downstream.Hello -> null
            is Downstream.Permission -> message.requestId
            is Downstream.Devices -> message.requestId
            is Downstream.Opened -> message.requestId
            is Downstream.Closed -> message.requestId
            is Downstream.Failure -> message.requestId
        }

        val waiter = requestId?.let { id ->
            synchronized(pending) { pending.firstOrNull { it.requestId == id }?.also { pending.remove(it) } }
        }

        when {
            waiter != null -> waiter.future.complete(message)
            message is Downstream.Devices -> onDevices(message)
            else -> Unit
        }
    }

    private fun <T : Upstream> request(timeoutMs: Long, message: (requestId: Int) -> T): Downstream {
        val id = nextRequestId.incrementAndGet()
        val upstream = message(id)
        val waiter = Pending(id, CompletableFuture())
        synchronized(pending) { pending.addLast(waiter) }

        try {
            send(upstream.toFrame())
            return waiter.future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            throw DeviceException("The helper did not answer $upstream", e)
        } finally {
            synchronized(pending) { pending.remove(waiter) }
        }
    }

    private fun drainPending(): List<Pending> = synchronized(pending) {
        pending.toList().also { pending.clear() }
    }

    private fun send(frame: Frame) = synchronized(writeLock) { writer.write(frame) }

    private inline fun <reified T : Downstream> expect(reply: Downstream): T = when (reply) {
        is T -> reply
        is Downstream.Failure -> throw DeviceException("${reply.code}: ${reply.message}")
        else -> throw DeviceException("The helper answered with an unexpected $reply")
    }
}
