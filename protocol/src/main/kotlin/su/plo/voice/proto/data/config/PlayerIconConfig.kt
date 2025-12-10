package su.plo.voice.proto.data.config

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import su.plo.slib.api.position.Pos3d
import su.plo.voice.proto.packets.PacketSerializable
import su.plo.voice.proto.packets.PacketUtil
import su.plo.voice.proto.serializer.Pos3dSerializer

class PlayerIconConfig : PacketSerializable {
    var iconVisibility: Set<PlayerIconVisibility>
        private set

    var iconOffset: Pos3d
        private set

    @JvmOverloads
    constructor(
        iconVisibility: Set<PlayerIconVisibility> = setOf(),
        iconOffset: Pos3d = Pos3d(),
    ) {
        this.iconVisibility = HashSet(iconVisibility)
        this.iconOffset = iconOffset
    }

    override fun deserialize(input: ByteArrayDataInput) {
        val iconVisibilitySize = PacketUtil.readSafeInt(input, 0, PlayerIconVisibility.entries.size)
        val iconVisibility = HashSet<PlayerIconVisibility>(iconVisibilitySize)

        (0 until iconVisibilitySize).forEach {
            iconVisibility.add(PlayerIconVisibility.valueOf(input.readUTF()))
        }

        this.iconVisibility = iconVisibility
        this.iconOffset = Pos3dSerializer.deserialize(input)
    }

    override fun serialize(out: ByteArrayDataOutput) {
        out.writeInt(iconVisibility.size)
        iconVisibility.forEach { out.writeUTF(it.name) }
        Pos3dSerializer.serialize(iconOffset, out)
    }

    override fun toString(): String =
        "PlayerIconConfig(iconVisibility=$iconVisibility, iconOffset=$iconOffset)"
}
