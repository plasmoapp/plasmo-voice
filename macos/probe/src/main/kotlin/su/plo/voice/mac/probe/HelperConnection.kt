package su.plo.voice.mac.probe

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameReader
import su.plo.voice.mac.protocol.frame.FrameWriter
import su.plo.voice.mac.protocol.message.Event
import su.plo.voice.mac.protocol.message.Request
import su.plo.voice.mac.protocol.message.wire.decode
import su.plo.voice.mac.protocol.message.wire.toFrame
import java.io.Closeable
import java.net.Socket
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reads / writes protocol frames over the socket, once the helper on the other end has proven it
 * is the process this connection was meant for.
 */
class HelperConnection private constructor(
    private val socket: Socket,
    private val reader: FrameReader,
    private val writer: FrameWriter,
    val pid: Int,
) : Closeable {
    private var nextRequestId = 0

    fun send(request: Request): Int = (++nextRequestId).also { writer.write(request.toFrame(it)) }

    fun read(): Frame? = reader.read()

    var timeout: Duration
        get() = socket.soTimeout.milliseconds
        set(value) { socket.soTimeout = value.inWholeMilliseconds.toInt() }

    override fun close() = socket.close()

    companion object {
        /**
         * Waits for the helper's hello and checks [token] against it, so a connection accepted on
         * the loopback port can't be mistaken for one from any other process on the machine.
         */
        fun handshake(socket: Socket, token: String): HelperConnection {
            val reader = FrameReader(socket.getInputStream().asSource().buffered())
            val writer = FrameWriter(socket.getOutputStream().asSink().buffered())

            val hello = (reader.read() as? Frame.Event)?.decode() as? Event.Hello
                ?: error("The helper did not say hello.")
            require(hello.token == token) { "The helper's token does not match." }

            return HelperConnection(socket, reader, writer, hello.pid)
        }
    }
}
