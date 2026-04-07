package su.plo.voice.client.render.voice

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.Colors
import su.plo.lib.mod.client.render.Colors.withAlpha
import su.plo.lib.mod.client.render.LazyGlState
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState

//#if MC>=1.21.11

//$$ import net.minecraft.client.gui.Font
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//$$ import net.minecraft.network.chat.Style
//$$ import net.minecraft.util.FormattedCharSequence
//$$ import net.minecraft.util.StringDecomposer

//#else

import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
import su.plo.lib.mod.client.render.pipeline.RenderPipelines.fromRenderType
import su.plo.lib.mod.extensions.rotate

//#if MC>=12000
//$$ import com.mojang.blaze3d.systems.RenderSystem
//#endif

//#endif

//#if MC>=12103
//$$ import net.minecraft.client.renderer.LightTexture
//#endif

object EntityIconRenderer {

    private val glState = LazyGlState()

    //#if MC>=1.21.11
    //$$ @JvmStatic
    //$$ fun render(
    //$$     entityState: LivingEntityRenderState,
    //$$     iconState: EntityVoiceIconState,
    //$$     cameraState: CameraRenderState,
    //$$     collector: SubmitNodeCollector,
    //$$     poseStack: PoseStack,
    //$$ ) {
    //$$     if (entityState.distanceToCameraSquared > 4096.0) return
    //$$
    //$$     renderPercent(entityState, iconState, cameraState, collector, poseStack)
    //$$     renderIcon(entityState, iconState, cameraState, collector, poseStack)
    //$$ }
    //#else
    @JvmStatic
    fun render(
        entityState: LivingEntityRenderState,
        iconState: EntityVoiceIconState,
        poseStack: PoseStack,
    ) {
        val camera = Minecraft.getInstance().gameRenderer.mainCamera

        if (entityState.distanceToCameraSquared > 4096.0) return

        renderPercent(entityState, iconState, camera, poseStack)
        renderIcon(entityState, iconState, camera, poseStack)
    }
    //#endif

    private fun renderPercent(
        entityState: LivingEntityRenderState,
        iconState: EntityVoiceIconState,
        //#if MC>=1.21.11
        //$$ cameraState: CameraRenderState,
        //$$ collector: SubmitNodeCollector,
        //#else
        camera: Camera,
        //#endif
        stack: PoseStack,
    ) {
        val text = iconState.percentText ?: return

        stack.pushPose()

        translateEntityMatrix(
            entityState,
            iconState,
            //#if MC>=1.21.11
            //$$ cameraState,
            //#else
            camera,
            //#endif
            stack,
        )

        val backgroundColor = (0.25f * 255.0f).toInt() shl 24
        val xOffset = -RenderUtil.getTextWidth(text) / 2

        //#if MC>=1.21.3
        //$$ val lightWithEmission = LightTexture.lightCoordsWithEmission(entityState.light, 2)
        //#else
        val lightWithEmission = entityState.light
        //#endif

        glState.withState {
            //#if MC>=1.21.11
            //$$ val formattedString = RenderUtil.getFormattedString(iconState.percentText)
            //$$ val charSequence =
            //$$     if (formattedString.isEmpty())
            //$$         FormattedCharSequence.EMPTY
            //$$     else
            //$$         FormattedCharSequence {
            //$$             StringDecomposer.iterateFormatted(formattedString, Style.EMPTY, it)
            //$$         }
            //$$
            //$$ if (!entityState.isDiscrete) {
            //$$    collector.submitText(
            //$$        stack,
            //$$        xOffset.toFloat(), 0f,
            //$$        charSequence,
            //$$        false,
            //$$        Font.DisplayMode.NORMAL,
            //$$        lightWithEmission,
            //$$        -1,
            //$$        0,
            //$$        0,
            //$$    )
            //$$ }
            //$$
            //$$ collector.submitText(
            //$$     stack,
            //$$     xOffset.toFloat(), 0f,
            //$$     charSequence,
            //$$     false,
            //$$     if (entityState.isDiscrete) Font.DisplayMode.NORMAL else Font.DisplayMode.SEE_THROUGH,
            //$$     entityState.light,
            //$$     Colors.WHITE.withAlpha(0.5f).rgb,
            //$$     backgroundColor,
            //$$     0,
            //$$ )
            //#else

            RenderUtil.drawStringLight(
                stack,
                text,
                xOffset,
                0,
                //#if MC>=12103
                //$$ Colors.WHITE.withAlpha(0.5f).rgb,
                //#else
                Colors.WHITE.withAlpha(0.13f).rgb,
                //#endif
                backgroundColor,
                entityState.light,
                !entityState.isDiscrete,
                false
            )

            if (!entityState.isDiscrete) {
                RenderUtil.drawStringLight(
                    stack,
                    text,
                    xOffset,
                    0,
                    -1,
                    0,
                    lightWithEmission,
                    false,
                    false,
                )
            }
            //#endif
        }

        stack.popPose()
    }

    private fun renderIcon(
        entityState: LivingEntityRenderState,
        iconState: EntityVoiceIconState,
        //#if MC>=1.21.11
        //$$ cameraState: CameraRenderState,
        //$$ collector: SubmitNodeCollector,
        //#else
        camera: Camera,
        //#endif
        stack: PoseStack,
    ) {
        val iconLocation = iconState.iconLocation ?: return

        stack.pushPose()

        if (iconState.percentText != null) stack.translate(0.0, 0.3, 0.0)
        translateEntityMatrix(
            entityState,
            iconState,
            //#if MC>=1.21.11
            //$$ cameraState,
            //#else
            camera,
            //#endif
            stack,
        )
        stack.translate(-5.0, 0.0, 0.0)

        if (entityState.isDiscrete) {
            vertices(
                //#if MC>=1.21.11
                //$$ collector,
                //#endif
                stack,
                40,
                entityState.light,
                iconLocation,
                false,
            )
        } else {
            //#if MC>=1.21.3
            //$$ val lightWithEmission = LightTexture.lightCoordsWithEmission(entityState.light, 2)
            //#else
            val lightWithEmission = entityState.light
            //#endif

            vertices(
                //#if MC>=1.21.11
                //$$ collector,
                //#endif
                stack,
                40,
                entityState.light,
                iconLocation,
                true,
            )
            vertices(
                //#if MC>=1.21.11
                //$$ collector,
                //#endif
                stack,
                255,
                lightWithEmission,
                iconLocation,
                false,
            )
        }

        stack.popPose()
    }

    private fun translateEntityMatrix(
        entityState: LivingEntityRenderState,
        iconState: EntityVoiceIconState,
        //#if MC>=1.21.11
        //$$ cameraState: CameraRenderState,
        //#else
        camera: Camera,
        //#endif
        stack: PoseStack,
    ) {
        val nameTagAttachment = entityState.nameTagAttachment.add(iconState.iconOffset)

        if (entityState.nameTag != null) {
            stack.translate(0.0, 0.3, 0.0)

            if (entityState.hasScoreboardText && entityState.distanceToCameraSquared < 100.0) {
                stack.translate(0.0, 0.3, 0.0)
            }
        }

        stack.translate(nameTagAttachment.x(), nameTagAttachment.y() + 0.5, nameTagAttachment.z())

        //#if MC>=1.21.11
        //$$ stack.mulPose(cameraState.orientation)
        //$$ stack.scale(0.025f, -0.025f, 0.025f)
        //#else
        stack.rotate(-camera.yRot, 0f, 1f, 0f)
        stack.rotate(camera.xRot, 1f, 0f, 0f)
        stack.scale(-0.025f, -0.025f, 0.025f)
        //#endif

        stack.translate(0.0, -1.0, 0.0)
    }

    private fun vertices(
        //#if MC>=1.21.11
        //$$ collector: SubmitNodeCollector,
        //#endif
        stack: PoseStack,
        alpha: Int,
        light: Int,
        iconLocation: ResourceLocation,
        seeThrough: Boolean,
    ) {
        //#if MC>=1.21.11

        //$$ val renderType =
        //$$     if (seeThrough)
        //$$         RenderTypes.textSeeThrough(iconLocation)
        //$$     else
        //$$         RenderTypes.text(iconLocation)
        //$$
        //$$ collector.submitCustomGeometry(stack, renderType) { stack, buffer ->
        //$$     vertex(stack, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
        //$$     vertex(stack, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
        //$$     vertex(stack, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
        //$$     vertex(stack, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)
        //$$ }

        //#else

        val renderType =
            if (seeThrough)
                RenderType.textSeeThrough(iconLocation)
            else
                RenderType.text(iconLocation)

        val renderPipeline = fromRenderType(
            if (seeThrough) "text_see_through" else "text",
            renderType
        )

        val buffer = RenderUtil.beginBuffer(renderPipeline)

        vertex(stack, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
        vertex(stack, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
        vertex(stack, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
        vertex(stack, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)

        //#if MC>=12100
        //$$ renderType.draw(buffer.build())
        //#elseif MC>=12000
        //$$ renderType.end(buffer, RenderSystem.getVertexSorting())
        //#else
        renderType.end(buffer, 0, 0, 0)
        //#endif

        //#endif
    }

    private fun vertex(
        //#if MC>=1.21.11
        //$$ stack: PoseStack.Pose,
        //#else
        stack: PoseStack,
        //#endif
        buffer: VertexConsumer,
        x: Float, y: Float, z: Float, u: Float, v: Float, alpha: Int, light: Int,
    ) {
        create(buffer)
            .position(stack, x, y, z)
            .color(255, 255, 255, alpha)
            .uv(u, v)
            .light(light)
            .normal(stack, 0f, 0f, -1f)
            .end()
    }
}
