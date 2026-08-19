package su.plo.voice.mac.protocol.frame

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import su.plo.voice.mac.protocol.exception.ProtocolError

private const val HEADER_SIZE = 5
private const val MAX_PAYLOAD_SIZE = 1 shl 20

/**
 * One message on the wire.
 */
class Frame(val type: FrameType, val payload: ByteArray)

/**
 * Writes frames as `type (1 byte) | payload size, big endian (4 bytes) | payload`.
 */
class FrameWriter(private val sink: Sink) {
    /**
     * Header and payload leave in one flush on purpose. Permission answers come from a system queue
     * and audio from the render thread, so two frames going out at once must not end up shredded
     * into each other.
     */
    fun write(frame: Frame) {
        sink.writeByte(frame.type.code)
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
        if (!source.request(HEADER_SIZE.toLong())) return null

        val code = source.readByte()
        val type = FrameType.byCode(code) ?: throw ProtocolError.UNKNOWN_FRAME_TYPE.exception(code)
        val size = source.readInt()
        if (size !in 0..MAX_PAYLOAD_SIZE) throw ProtocolError.FRAME_SIZE_OUT_OF_BOUNDS.exception(size)

        if (!source.request(size.toLong())) return null

        return Frame(type, source.readByteArray(size))
    }
}
