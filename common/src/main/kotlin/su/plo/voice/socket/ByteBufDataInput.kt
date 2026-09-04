package su.plo.voice.socket

import com.google.common.io.ByteArrayDataInput
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import java.io.DataInputStream
import kotlin.math.min

class ByteBufDataInput(
    private val buffer: ByteBuf,
) : ByteArrayDataInput {
    private var input: DataInputStream? = null

    override fun readFully(b: ByteArray) {
        buffer.readBytes(b)
    }

    override fun readFully(b: ByteArray, off: Int, len: Int) {
        buffer.readBytes(b, off, len)
    }

    override fun skipBytes(n: Int): Int {
        val skipped = min(n, buffer.readableBytes())
        buffer.skipBytes(skipped)
        return skipped
    }

    override fun readBoolean(): Boolean =
        buffer.readBoolean()

    override fun readByte(): Byte =
        buffer.readByte()

    override fun readUnsignedByte(): Int =
        buffer.readUnsignedByte().toInt()

    override fun readShort(): Short =
        buffer.readShort()

    override fun readUnsignedShort(): Int =
        buffer.readUnsignedShort()

    override fun readChar(): Char =
        buffer.readChar()

    override fun readInt(): Int =
        buffer.readInt()

    override fun readLong(): Long =
        buffer.readLong()

    override fun readFloat(): Float =
        buffer.readFloat()

    override fun readDouble(): Double =
        buffer.readDouble()

    @Suppress("Deprecation")
    override fun readLine(): String? =
        input().readLine()

    override fun readUTF(): String =
        input().readUTF()

    private fun input(): DataInputStream {
        if (input == null) {
            input = DataInputStream(ByteBufInputStream(buffer))
        }

        return input!!
    }
}
