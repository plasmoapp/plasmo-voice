package su.plo.voice.client.render.voice

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.Colors.withAlpha
import su.plo.lib.mod.client.render.LazyGlState
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.entity.EntityRenderState

//#if MC<1.21.11
import su.plo.lib.mod.extensions.rotate
//#endif

//#if MC>=12103
//$$ import net.minecraft.client.renderer.LightTexture
//#endif

object EntityIconRenderer {

    private val glState = LazyGlState()

    //#if MC>=12103
    //$$ private val PERCENT_TEXT_COLOR = Colors.WHITE.withAlpha(0.5f).rgb
    //#else
    private val PERCENT_TEXT_COLOR = Colors.WHITE.withAlpha(0.13f).rgb
    //#endif

    @JvmStatic
    fun render(
        entityState: EntityRenderState,
        iconState: EntityVoiceIconState,
        context: EntityIconRenderContext,
    ) {
        if (entityState.distanceToCameraSquared > 4096.0) return

        renderPercent(entityState, iconState, context)
        renderIcon(entityState, iconState, context)
    }

    private fun renderPercent(
        entityState: EntityRenderState,
        iconState: EntityVoiceIconState,
        context: EntityIconRenderContext,
    ) {
        val text = iconState.percentText ?: return
        val stack = context.stack

        stack.pushPose()

        translateEntityMatrix(entityState, iconState, context)

        val backgroundColor = (0.25f * 255.0f).toInt() shl 24
        val xOffset = -RenderUtil.getTextWidth(text) / 2

        //#if MC>=1.21.3
        //$$ val lightWithEmission = LightTexture.lightCoordsWithEmission(entityState.light, 2)
        //#else
        val lightWithEmission = entityState.light
        //#endif

        glState.withState {
            context.submitText(
                text,
                xOffset, 0,
                PERCENT_TEXT_COLOR,
                backgroundColor,
                entityState.light,
                seeThrough = !entityState.isDiscrete,
            )

            if (!entityState.isDiscrete) {
                context.submitText(
                    text,
                    xOffset, 0,
                    -1, 0,
                    lightWithEmission,
                    seeThrough = false,
                )
            }
        }

        stack.popPose()
    }

    private fun renderIcon(
        entityState: EntityRenderState,
        iconState: EntityVoiceIconState,
        context: EntityIconRenderContext,
    ) {
        val iconLocation = iconState.iconLocation ?: return
        val stack = context.stack

        stack.pushPose()

        if (iconState.percentText != null) stack.translate(0.0, 0.3, 0.0)
        translateEntityMatrix(entityState, iconState, context)
        stack.translate(-5.0, 0.0, 0.0)

        if (entityState.isDiscrete) {
            vertices(context, 40, entityState.light, iconLocation, false)
        } else {
            //#if MC>=1.21.3
            //$$ val lightWithEmission = LightTexture.lightCoordsWithEmission(entityState.light, 2)
            //#else
            val lightWithEmission = entityState.light
            //#endif

            vertices(context, 40, entityState.light, iconLocation, true)
            vertices(context, 255, lightWithEmission, iconLocation, false)
        }

        stack.popPose()
    }

    private fun translateEntityMatrix(
        entityState: EntityRenderState,
        iconState: EntityVoiceIconState,
        context: EntityIconRenderContext,
    ) {
        val stack = context.stack
        val nameTagAttachment = entityState.nameTagAttachment.add(iconState.iconOffset)

        if (entityState.nameTag != null) {
            stack.translate(0.0, 0.3, 0.0)

            if (entityState.hasScoreboardText && entityState.distanceToCameraSquared < 100.0) {
                stack.translate(0.0, 0.3, 0.0)
            }
        }

        stack.translate(nameTagAttachment.x(), nameTagAttachment.y() + 0.5, nameTagAttachment.z())

        //#if MC>=1.21.11
        //$$ stack.mulPose(context.camera.orientation)
        //$$ stack.scale(0.025f, -0.025f, 0.025f)
        //#else
        stack.rotate(-context.camera.yRot, 0f, 1f, 0f)
        stack.rotate(context.camera.xRot, 1f, 0f, 0f)
        stack.scale(-0.025f, -0.025f, 0.025f)
        //#endif

        stack.translate(0.0, -1.0, 0.0)
    }

    private fun vertices(
        context: EntityIconRenderContext,
        alpha: Int,
        light: Int,
        iconLocation: ResourceLocation,
        seeThrough: Boolean,
    ) {
        context.submitTexturedGeometry(iconLocation, seeThrough) { pose, buffer ->
            quad(pose, buffer, alpha, light)
        }
    }

    private fun quad(pose: PoseStack.Pose, buffer: VertexConsumer, alpha: Int, light: Int) {
        vertex(pose, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
        vertex(pose, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
        vertex(pose, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
        vertex(pose, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)
    }

    private fun vertex(
        pose: PoseStack.Pose,
        buffer: VertexConsumer,
        x: Float, y: Float, z: Float, u: Float, v: Float, alpha: Int, light: Int,
    ) {
        create(buffer)
            .position(pose, x, y, z)
            .color(255, 255, 255, alpha)
            .uv(u, v)
            .light(light)
            .normal(pose, 0f, 0f, -1f)
            .end()
    }
}
