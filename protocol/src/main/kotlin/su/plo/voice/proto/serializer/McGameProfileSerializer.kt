package su.plo.voice.proto.serializer

import com.google.common.base.Preconditions
import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import su.plo.slib.api.entity.player.McGameProfile
import su.plo.voice.proto.packets.PacketUtil
import java.io.IOException

object McGameProfileSerializer : PacketSerializer<McGameProfile> {

    @Throws(IOException::class)
    override fun deserialize(buffer: ByteArrayDataInput): McGameProfile {
        val id = PacketUtil.readUUID(buffer)
        val name = buffer.readUTF()

        val length = PacketUtil.readSafeInt(buffer, 0, 100)
        val properties = ArrayList<McGameProfile.Property>()
        for (i in 0 until length) {
            properties.add(
                McGameProfile.Property(
                    buffer.readUTF(),
                    buffer.readUTF(),
                    buffer.readUTF()
                )
            )
        }

        return McGameProfile(id, name, properties)
    }

    @Throws(IOException::class)
    override fun serialize(obj: McGameProfile, buffer: ByteArrayDataOutput) {
        PacketUtil.writeUUID(buffer, Preconditions.checkNotNull(obj.id))
        buffer.writeUTF(Preconditions.checkNotNull(obj.name))

        buffer.writeInt(obj.properties.size)
        for ((propertyName, propertyValue, propertySignature) in obj.properties) {
            buffer.writeUTF(propertyName)
            buffer.writeUTF(propertyValue)
            buffer.writeUTF(propertySignature ?: "")
        }
    }
}
