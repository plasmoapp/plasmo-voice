package su.plo.voice.mac.protocol.frame

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import su.plo.voice.mac.protocol.exception.ProtocolError

private const val MAX_PAYLOAD_SIZE = 1 shl 20

sealed interface Frame {
    val payload: ByteArray

    /**
     * Something the mod asks for. The helper answers every one with exactly one [Response] carrying
     * the same [requestId], so the mod never has to guess which answer belongs to which question.
     */
    class Request(val requestId: Int, override val payload: ByteArray) : Frame

    /** The answer to the [Request] with the same [requestId]. */
    class Response(val requestId: Int, override val payload: ByteArray) : Frame

    /** Something the helper says without being asked. */
    class Event(override val payload: ByteArray) : Frame

    /** One capture frame. */
    class Audio(override val payload: ByteArray) : Frame

    /**
     * A dead helper is noticed the moment the socket closes, but a wedged one never closes
     * anything, and this is how that gets caught.
     */
    data object Ping : Frame {
        override val payload = ByteArray(0)
    }
}

/**
 * Writes frames as `type (1 byte) | request id (4 bytes, requests and responses only) |
 * payload size (4 bytes) | payload`, big endian.
 */
class FrameWriter(private val sink: Sink) {
    /**
     * Not thread-safe, callers must ensure thread-safety themselves.
     */
    fun write(frame: Frame) {
        sink.writeByte(frame.type.code)
        when (frame) {
            is Frame.Request -> sink.writeInt(frame.requestId)
            is Frame.Response -> sink.writeInt(frame.requestId)
            is Frame.Event, is Frame.Audio, Frame.Ping -> Unit
        }
        sink.writeInt(frame.payload.size)
        sink.write(frame.payload)
        sink.flush()
    }
}

/**
 * Reads frames written by [FrameWriter].
 */
class FrameReader(private val source: Source) {
    /**
     * Returns null once the other side is gone.
     */
    fun read(): Frame? {
        if (!source.request(1)) return null

        val code = source.readByte()
        val type = FrameType.byCode(code) ?: throw ProtocolError.UNKNOWN_FRAME_TYPE.exception(code)

        val requestId = if (type.hasRequestId) readInt() ?: return null else 0
        val size = readInt() ?: return null
        if (size !in 0..MAX_PAYLOAD_SIZE) throw ProtocolError.FRAME_SIZE_OUT_OF_BOUNDS.exception(size)

        if (!source.request(size.toLong())) return null
        val payload = source.readByteArray(size)

        return when (type) {
            FrameType.REQUEST -> Frame.Request(requestId, payload)
            FrameType.RESPONSE -> Frame.Response(requestId, payload)
            FrameType.EVENT -> Frame.Event(payload)
            FrameType.AUDIO -> Frame.Audio(payload)
            FrameType.PING -> Frame.Ping
        }
    }

    private fun readInt(): Int? = if (source.request(Int.SIZE_BYTES.toLong())) source.readInt() else null
}

private val Frame.type: FrameType
    get() = when (this) {
        is Frame.Request -> FrameType.REQUEST
        is Frame.Response -> FrameType.RESPONSE
        is Frame.Event -> FrameType.EVENT
        is Frame.Audio -> FrameType.AUDIO
        Frame.Ping -> FrameType.PING
    }
