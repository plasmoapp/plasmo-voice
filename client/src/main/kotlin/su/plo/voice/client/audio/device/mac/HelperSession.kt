package su.plo.voice.client.audio.device.mac

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.util.AudioUtil
import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameWriter
import su.plo.voice.mac.protocol.message.Devices
import su.plo.voice.mac.protocol.message.Event
import su.plo.voice.mac.protocol.message.Request
import su.plo.voice.mac.protocol.message.Response
import su.plo.voice.mac.protocol.message.status.AuthStatus
import su.plo.voice.mac.protocol.message.wire.PROTOCOL_VERSION
import su.plo.voice.mac.protocol.message.wire.decode
import su.plo.voice.mac.protocol.message.wire.toFrame
import java.io.Closeable
import java.io.IOException
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

private const val REPLY_TIMEOUT_MS = 5_000L
private const val PROMPT_TIMEOUT_MS = 120_000L
private const val PING_INTERVAL_MS = 5_000L
private const val WEDGE_TIMEOUT_MS = 15_000L

/**
 * A live connection to helper process.
 *
 * [onAudio] and [onDevices] are called on the reader thread.
 */
internal class HelperSession(private val socket: Socket, token: String) : Closeable {
    private val frames = FrameReader(socket.getInputStream().asSource().buffered())
    private val writer = FrameWriter(socket.getOutputStream().asSink().buffered())
    private val pending = ConcurrentHashMap<Int, CompletableFuture<Response>>()
    private val writeLock = Any()
    private val nextRequestId = AtomicInteger()

    @Volatile
    var onAudio: (ShortArray) -> Unit = {}

    @Volatile
    var onDevices: (Devices) -> Unit = {}

    @Volatile
    var alive = true; private set

    @Volatile
    private var lastFrameAt = System.currentTimeMillis()

    val pid: Int

    init {
        val hello = (frames.read() as? Frame.Event)?.decode() as? Event.Hello
            ?: throw IOException("The helper did not say hello")
        if (hello.token != token) throw IOException("The helper's token does not match")
        if (hello.version != PROTOCOL_VERSION) {
            throw IOException("The helper speaks protocol ${hello.version}, expected $PROTOCOL_VERSION")
        }

        pid = hello.pid
        thread(name = "Plasmo Voice Microphone", isDaemon = true, block = ::pump)
        thread(name = "Plasmo Voice Microphone Watchdog", isDaemon = true, block = ::watchdog)
    }

    fun openMicrophone(format: CaptureFormat, deviceId: String?): Int =
        expect<Response.Opened>(request(Request.Open(format, deviceId))).frameSamples

    fun closeMicrophone() {
        expect<Response.Closed>(request(Request.Close))
    }

    fun listDevices(): Devices =
        expect(request(Request.ListDevices))

    fun permission(prompt: Boolean): AuthStatus =
        expect<Response.Permission>(
            request(Request.Permission(prompt), if (prompt) PROMPT_TIMEOUT_MS else REPLY_TIMEOUT_MS)
        ).status

    fun openSettings() {
        expect<Response.SettingsOpened>(request(Request.OpenSettings))
    }

    override fun close() {
        alive = false
        runCatching { socket.close() }
    }

    private fun pump() {
        try {
            while (true) {
                val frame = frames.read() ?: break
                lastFrameAt = System.currentTimeMillis()

                when (frame) {
                    is Frame.Audio -> onAudio(AudioUtil.bytesToShorts(frame.payload))
                    is Frame.Response -> pending[frame.requestId]?.complete(frame.decode())
                    is Frame.Event -> handle(frame.decode())
                    is Frame.Request, Frame.Ping -> Unit
                }
            }
        } catch (_: Exception) {
        } finally {
            alive = false
            pending.values.forEach { it.completeExceptionally(IOException("The helper is gone")) }
            runCatching { socket.close() }
        }
    }

    private fun watchdog() {
        while (alive) {
            Thread.sleep(PING_INTERVAL_MS)

            val idleFor = System.currentTimeMillis() - lastFrameAt
            when {
                idleFor >= WEDGE_TIMEOUT_MS -> close()
                idleFor >= PING_INTERVAL_MS -> runCatching { send(Frame.Ping) }
            }
        }
    }

    private fun handle(event: Event) = when (event) {
        is Devices -> onDevices(event)
        is Event.Hello -> Unit
    }

    private fun request(request: Request, timeoutMs: Long = REPLY_TIMEOUT_MS): Response {
        val requestId = nextRequestId.incrementAndGet()
        val reply = CompletableFuture<Response>()
        pending[requestId] = reply

        try {
            send(request.toFrame(requestId))
            return reply.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            throw DeviceException("The helper did not answer $request", e)
        } finally {
            pending.remove(requestId)
        }
    }

    private fun send(frame: Frame) = synchronized(writeLock) { writer.write(frame) }

    private inline fun <reified T : Response> expect(reply: Response): T = when (reply) {
        is T -> reply
        is Response.Failure -> throw DeviceException("${reply.code}: ${reply.message}")
        else -> throw DeviceException("The helper answered with an unexpected $reply")
    }
}
