package su.plo.lib.mod.client.render.entity

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import su.plo.voice.proto.data.config.PlayerIconVisibility
import java.util.UUID

data class LivingEntityRenderState(
    val entityId: Int,
    val entityUUID: UUID,
    val entityType: EntityType<*>,
    val x: Double,
    val y: Double,
    val z: Double,
    val distanceToCameraSquared: Double,
    val isInvisibleToPlayer: Boolean,
    val isDiscrete: Boolean,
    val nameTag: Component?,
    val nameTagAttachment: Vec3,
    val hasScoreboardText: Boolean, // todo: this is actually player only

    val shouldHideIcon: Boolean,
    val playerIconVisibility: Set<PlayerIconVisibility>,
)
