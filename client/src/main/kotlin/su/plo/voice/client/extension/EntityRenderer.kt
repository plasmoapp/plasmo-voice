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
    val distanceToCameraSquared = camera.position.distanceToSqr(entity.position())

    val customName = entity.customName?.toString()
    val shouldHideIcon = customName?.contains("plasmo-voice.hide-all-icons") ?: false
    val shouldHideNotInstalledIcon = customName?.contains("plasmo-voice.hide-not-installed-icon") ?: false

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
        Vec3(0.0, entity.bbHeight.toDouble(), 0.0),
        hasScoreboardText,

        shouldHideIcon,
        shouldHideNotInstalledIcon,
    )

    return entityRenderState
}
