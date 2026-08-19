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
import su.plo.voice.mac.protocol.message.status.AuthStatus
import su.plo.voice.mac.protocol.message.Downstream
import su.plo.voice.mac.protocol.message.wire.PROTOCOL_VERSION
import su.plo.voice.mac.protocol.message.Upstream
import su.plo.voice.mac.protocol.message.wire.toDownstream
import su.plo.voice.mac.protocol.message.wire.toFrame
import java.io.Closeable
import java.io.IOException
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

private const val REPLY_TIMEOUT_MS = 5_000L
private const val PROMPT_TIMEOUT_MS = 120_000L

/**
 * A live connection to one helper process.
 *
 * Audio arrives on a reader thread and is handed to [onAudio]. Everything else is just
 * request / response.
 */
internal class HelperSession(private val socket: Socket, token: String) : Closeable {
    private val frames = FrameReader(socket.getInputStream().asSource().buffered())
    private val writer = FrameWriter(socket.getOutputStream().asSink().buffered())
    private val pending = AtomicReference<Pending?>()
    private val writeLock = Any()
    private val requestLock = Any()

    private class Pending(val expectsDevices: Boolean, val future: CompletableFuture<Downstream>)

    @Volatile
    var onAudio: (ShortArray) -> Unit = {}

    @Volatile
    var onDevices: (Downstream.Devices) -> Unit = {}

    @Volatile
    var alive = true; private set

    val pid: Int

    init {
        val hello = frames.read()?.toDownstream() as? Downstream.Hello ?: throw IOException("The helper did not say hello.")
        if (hello.token != token) throw IOException("The helper's token does not match.")
        if (hello.version != PROTOCOL_VERSION) {
            throw IOException("The helper speaks protocol ${hello.version}, expected $PROTOCOL_VERSION.")
        }

        pid = hello.pid
        thread(name = "Plasmo Voice Microphone", isDaemon = true, block = ::pump)
    }

    /** @return how many samples every audio frame carries. */
    fun openMicrophone(format: CaptureFormat, deviceId: String?): Int =
        expect<Downstream.Opened>(request(Upstream.Open(format, deviceId), REPLY_TIMEOUT_MS)).frameSamples

    /** Closes the microphone. */
    fun closeMicrophone() {
        expect<Downstream.Closed>(request(Upstream.Close, REPLY_TIMEOUT_MS))
    }

    fun listDevices(): Downstream.Devices =
        expect(request(Upstream.ListDevices, REPLY_TIMEOUT_MS))

    /** Asking with [prompt] may put a system dialog in front of the player. */
    fun permission(prompt: Boolean): AuthStatus =
        expect<Downstream.Permission>(
            request(Upstream.Permission(prompt), if (prompt) PROMPT_TIMEOUT_MS else REPLY_TIMEOUT_MS)
        ).status

    /** Makes sure the microphone is actually usable before anything tries to open it. */
    fun grantPermission() {
        val status = permission(prompt = false)
        val granted = if (status == AuthStatus.NOT_DETERMINED) permission(prompt = true) else status

        if (granted != AuthStatus.AUTHORIZED) throw DeviceException("Microphone access is $granted.")
    }

    /** Opens settings. */
    fun openSettings() = send(Upstream.OpenSettings.toFrame())

    override fun close() {
        alive = false
        runCatching { socket.close() }
    }

    private fun pump() {
        try {
            while (true) {
                val frame = frames.read() ?: break
                when (frame.type) {
                    FrameType.AUDIO -> onAudio(AudioUtil.bytesToShorts(frame.payload))
                    FrameType.CONTROL -> {
                        val message = frame.toDownstream()

                        if (message is Downstream.Devices && pending.get()?.expectsDevices != true) {
                            onDevices(message)
                        } else {
                            pending.getAndSet(null)?.future?.complete(message)
                        }
                    }
                    FrameType.PING -> send(frame)
                }
            }
        } catch (_: Exception) {
        } finally {
            alive = false
            pending.getAndSet(null)?.future?.completeExceptionally(IOException("The helper is gone."))
            runCatching { socket.close() }
        }
    }

    private fun request(message: Upstream, timeoutMs: Long): Downstream = synchronized(requestLock) {
        val waiter = Pending(message is Upstream.ListDevices, CompletableFuture())
        pending.set(waiter)

        try {
            send(message.toFrame())
            waiter.future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            throw DeviceException("The helper did not answer $message.", e)
        } finally {
            pending.compareAndSet(waiter, null)
        }
    }

    private fun send(frame: Frame) = synchronized(writeLock) { writer.write(frame) }

    private inline fun <reified T : Downstream> expect(reply: Downstream): T = when (reply) {
        is T -> reply
        is Downstream.Failure -> throw DeviceException("${reply.code}: ${reply.message}.")
        else -> throw DeviceException("The helper answered with an unexpected $reply.")
    }
}
