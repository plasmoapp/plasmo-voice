package su.plo.lib.mod.client.render.entity


import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3
//#if MC>=1.21.2
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState
//$$ import net.minecraft.client.renderer.entity.state.PlayerRenderState
//$$
//$$ class LivingEntityRenderState(
//$$     private val entityState: EntityRenderState,
//#if MC<1.21.9
//$$     val light: Int,
//#endif
//$$ ) {
//$$     val distanceToCameraSquared: Double
//$$         get() = entityState.distanceToCameraSq
//$$     val isDiscrete: Boolean
//$$         get() = entityState.isDiscrete
//$$     val nameTag: Component?
//$$         get() = entityState.nameTag
//$$     val nameTagAttachment: Vec3 =
//$$         entityState.nameTagAttachment ?: Vec3(0.0, entityState.boundingBoxHeight.toDouble(), 0.0)
//$$     val hasScoreboardText: Boolean
//$$         get() =
//$$             if (entityState is PlayerRenderState)
//$$                 entityState.scoreText != null
//$$             else
//$$                 false
//#if MC>=1.21.9
//$$     val light: Int
//$$         get() = entityState.lightCoords
//#endif
//$$ }
//#else
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.scores.Scoreboard
import su.plo.lib.mod.extensions.level
import su.plo.voice.client.extension.position
import su.plo.voice.client.mixin.accessor.EntityRendererAccessor

//#if MC>=12002
//$$ import net.minecraft.world.scores.DisplaySlot
//#endif

data class LivingEntityRenderState(
    val distanceToCameraSquared: Double,
    val isDiscrete: Boolean,
    val nameTag: Component?,
    val nameTagAttachment: Vec3,
    val hasScoreboardText: Boolean,
    val light: Int,
)

private fun Scoreboard.getObjectiveBelowName() =
//#if MC>=12002
//$$ getDisplayObjective(DisplaySlot.BELOW_NAME)
//#else
    getDisplayObjective(2)
//#endif

fun EntityRenderer<*>.createEntityRenderState(
    entity: LivingEntity,
    light: Int,
): LivingEntityRenderState {
    val hasScoreboardText = (entity as? Player)?.level()?.scoreboard?.getObjectiveBelowName() != null

    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val distanceToCameraSquared = camera.position().distanceToSqr(entity.position())

    val rendererAccessor = this as EntityRendererAccessor
    val entityRenderState = LivingEntityRenderState(
        distanceToCameraSquared,
        entity.isDiscrete,
        if (rendererAccessor.plasmovoice_shouldShowName(entity))
            entity.displayName
        else
            null,
        Vec3(0.0, entity.bbHeight.toDouble(), 0.0),
        hasScoreboardText,
        light,
    )

    return entityRenderState
}

//#endif
