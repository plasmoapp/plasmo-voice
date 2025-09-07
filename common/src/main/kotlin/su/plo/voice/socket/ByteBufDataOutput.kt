package su.plo.voice.socket

import com.google.common.io.ByteArrayDataOutput
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import java.io.DataOutputStream
import java.nio.charset.StandardCharsets

class ByteBufDataOutput(
    private val buffer: ByteBuf,
) : ByteArrayDataOutput {
    private var output: DataOutputStream? = null

    override fun write(b: Int) {
        buffer.writeByte(b)
    }

    override fun write(b: ByteArray) {
        buffer.writeBytes(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.writeBytes(b, off, len)
    }

    override fun writeBoolean(v: Boolean) {
        buffer.writeBoolean(v)
    }

    override fun writeByte(v: Int) {
        buffer.writeByte(v)
    }

    override fun writeShort(v: Int) {
        buffer.writeShort(v)
    }

    override fun writeChar(v: Int) {
        buffer.writeChar(v)
    }

    override fun writeInt(v: Int) {
        buffer.writeInt(v)
    }

    override fun writeLong(v: Long) {
        buffer.writeLong(v)
    }

    override fun writeFloat(v: Float) {
        buffer.writeFloat(v)
    }

    override fun writeDouble(v: Double) {
        buffer.writeDouble(v)
    }

    @Deprecated("")
    override fun writeBytes(s: String) {
        buffer.writeCharSequence(s, StandardCharsets.ISO_8859_1)
    }

    override fun writeChars(s: String) {
        s.chars().forEach(buffer::writeChar)
    }

    override fun writeUTF(s: String) {
        if (output == null) {
            output = DataOutputStream(ByteBufOutputStream(buffer))
        }

        output!!.writeUTF(s)
    }

    override fun toByteArray(): ByteArray {
        val arr = ByteArray(buffer.readableBytes())
        buffer.getBytes(buffer.readerIndex(), arr)
        return arr
    }
}
