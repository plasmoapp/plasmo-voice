package su.plo.voice.proto.data.config

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import su.plo.slib.api.position.Pos3d
import su.plo.voice.proto.packets.PacketSerializable
import su.plo.voice.proto.packets.PacketUtil
import su.plo.voice.proto.serializer.Pos3dSerializer
import kotlin.jvm.internal.DefaultConstructorMarker

class PlayerIconConfig : PacketSerializable {
    var iconVisibility: Set<PlayerIconVisibility>
        private set

    var iconOffset: Pos3d
        private set

    constructor(
        iconVisibility: Set<PlayerIconVisibility>,
        iconOffset: Pos3d,
    ) {
        this.iconVisibility = HashSet(iconVisibility)
        this.iconOffset = iconOffset
    }

    constructor(iconVisibility: Set<PlayerIconVisibility>) : this(iconVisibility, Pos3d())

    constructor() : this(setOf())

    @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        iconVisibility: Set<PlayerIconVisibility>?,
        iconOffset: Pos3d?,
        defaultsMask: Int,
        marker: DefaultConstructorMarker?,
    ) : this(
        if (defaultsMask and 0x1 != 0) setOf() else iconVisibility!!,
        if (defaultsMask and 0x2 != 0) Pos3d() else iconOffset!!,
    )

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
