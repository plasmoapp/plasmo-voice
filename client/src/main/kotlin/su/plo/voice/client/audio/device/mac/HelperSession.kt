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
import su.plo.voice.mac.protocol.message.AuthStatus
import su.plo.voice.mac.protocol.message.Downstream
import su.plo.voice.mac.protocol.message.PROTOCOL_VERSION
import su.plo.voice.mac.protocol.message.Upstream
import su.plo.voice.mac.protocol.message.toDownstream
import su.plo.voice.mac.protocol.message.toFrame
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
    private val pending = AtomicReference<CompletableFuture<Downstream>?>()
    private val writeLock = Any()
    private val requestLock = Any()

    @Volatile
    var onAudio: (ShortArray) -> Unit = {}

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
    fun openMicrophone(format: CaptureFormat): Int =
        expect<Downstream.Opened>(request(Upstream.Open(format), REPLY_TIMEOUT_MS)).frameSamples

    /** Closes the microphone. */
    fun closeMicrophone() {
        expect<Downstream.Closed>(request(Upstream.Close, REPLY_TIMEOUT_MS))
    }

    /** Asking with [prompt] may put a system dialog in front of the player. */
    fun permission(prompt: Boolean): AuthStatus =
        expect<Downstream.Permission>(
            request(Upstream.Permission(prompt), if (prompt) PROMPT_TIMEOUT_MS else REPLY_TIMEOUT_MS)
        ).status

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
                    FrameType.CONTROL -> pending.getAndSet(null)?.complete(frame.toDownstream())
                    FrameType.PING -> send(frame)
                }
            }
        } catch (_: Exception) {
        } finally {
            alive = false
            pending.getAndSet(null)?.completeExceptionally(IOException("The helper is gone."))
            runCatching { socket.close() }
        }
    }

    private fun request(message: Upstream, timeoutMs: Long): Downstream = synchronized(requestLock) {
        val future = CompletableFuture<Downstream>()
        pending.set(future)

        try {
            send(message.toFrame())
            future.get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            throw DeviceException("The helper did not answer $message.", e)
        } finally {
            pending.compareAndSet(future, null)
        }
    }

    private fun send(frame: Frame) = synchronized(writeLock) { writer.write(frame) }

    private inline fun <reified T : Downstream> expect(reply: Downstream): T = when (reply) {
        is T -> reply
        is Downstream.Failure -> throw DeviceException("${reply.code}: ${reply.message}.")
        else -> throw DeviceException("The helper answered with an unexpected $reply.")
    }
}
