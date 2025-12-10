package su.plo.voice.client.extension

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState
import su.plo.voice.client.mixin.accessor.EntityRendererAccessor
import net.minecraft.world.scores.Scoreboard
import su.plo.lib.mod.extensions.level
import su.plo.slib.api.position.Pos3d
import su.plo.voice.client.ModVoiceClient
import su.plo.voice.proto.data.config.PlayerIconVisibility
import java.util.EnumSet
import kotlin.jvm.optionals.getOrNull

//#if MC>=12002
//$$ import net.minecraft.world.scores.DisplaySlot
//#endif

private fun Scoreboard.getObjectiveBelowName() =
//#if MC>=12002
//$$ getDisplayObjective(DisplaySlot.BELOW_NAME)
    //#else
    getDisplayObjective(2)
//#endif

//#if MC>=12102
//$$ fun EntityRenderer<*, *>.createEntityRenderState(
//#else
fun EntityRenderer<*>.createEntityRenderState(
//#endif
    clientPlayer: LocalPlayer,
    entity: LivingEntity,
): LivingEntityRenderState {
    val hasScoreboardText = (entity as? Player)?.level()?.scoreboard?.getObjectiveBelowName() != null

    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val distanceToCameraSquared = camera.position().distanceToSqr(entity.position())

    val customName = entity.customName?.toString()

    var playerIconVisibility = getPlayerIconVisibility(entity)

    val shouldHideIcon = customName?.contains("plasmo-voice.hide-all-icons") == true

    if (customName?.contains("plasmo-voice.hide-not-installed-icon") == true) {
        playerIconVisibility = EnumSet.copyOf(
            playerIconVisibility + setOf(PlayerIconVisibility.HIDE_NOT_INSTALLED)
        )
    }

    val playerIconOffset = getPlayerIconOffset(entity)

    val rendererAccessor = this as EntityRendererAccessor
    val entityRenderState = LivingEntityRenderState(
        entity.id,
        entity.uuid,
        entity.type,
        entity.position().x(),
        entity.position().y(),
        entity.position().z(),
        distanceToCameraSquared,
        entity.isInvisibleTo(clientPlayer),
        entity.isDiscrete,
        //#if MC>=12102
        //$$ if (rendererAccessor.plasmovoice_shouldShowName(entity, distanceToCameraSquared))
        //#else
        if (rendererAccessor.plasmovoice_shouldShowName(entity))
        //#endif
            entity.displayName
        else
            null,
        Vec3(playerIconOffset.x, entity.bbHeight.toDouble() + playerIconOffset.y, playerIconOffset.z),
        hasScoreboardText,

        shouldHideIcon,
        playerIconVisibility,
    )

    return entityRenderState
}

private val pos3dZero = Pos3d()

private fun getPlayerIconOffset(entity: LivingEntity): Pos3d {
    if (entity !is Player) return pos3dZero
    val serverInfo = ModVoiceClient.INSTANCE.serverInfo.getOrNull() ?: return pos3dZero

    return serverInfo.playerIconOffset
}

private fun getPlayerIconVisibility(entity: LivingEntity): Set<PlayerIconVisibility> {
    if (entity !is Player) return PlayerIconVisibility.none()

    val serverInfo = ModVoiceClient.INSTANCE.serverInfo.getOrNull() ?: return PlayerIconVisibility.none()

    return serverInfo.getPlayerIconVisibility()
}
