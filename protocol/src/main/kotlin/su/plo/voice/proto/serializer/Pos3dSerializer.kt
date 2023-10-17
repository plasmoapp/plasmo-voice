package su.plo.voice.proto.serializer

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import su.plo.slib.api.position.Pos3d
import java.io.IOException

object Pos3dSerializer : PacketSerializer<Pos3d> {

    @Throws(IOException::class)
    override fun deserialize(buffer: ByteArrayDataInput) =
        Pos3d(
            buffer.readDouble(),
            buffer.readDouble(),
            buffer.readDouble()
        )

    @Throws(IOException::class)
    override fun serialize(obj: Pos3d, buffer: ByteArrayDataOutput) {
        buffer.writeDouble(obj.x)
        buffer.writeDouble(obj.y)
        buffer.writeDouble(obj.z)
    }
}
