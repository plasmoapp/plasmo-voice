package su.plo.voice.client.render.level

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.lib.mod.client.render.VertexBuilder.Companion.create
import su.plo.lib.mod.client.render.level.LevelRenderContext
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
import su.plo.lib.mod.extensions.rotate
import su.plo.voice.client.event.LevelRenderEvent
import su.plo.voice.client.extension.position

//#if MC>=12111
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes
//#endif

//#if MC>=12103 && MC<26.1
//$$ import net.minecraft.client.renderer.LightTexture
//#endif

//#if MC>=26.2
//$$ import net.minecraft.client.renderer.SubmitNodeCollection
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.feature.CustomFeatureRenderer
//#endif

class StaticSourceIconRenderer : LevelRenderEvent.Callback {

    override fun onRender(context: LevelRenderContext) {
        val entries = context.state.get<StaticSourceIconState>(STATIC_SOURCE_ICON_STATE_KEY)?.entries
            ?: return

        entries.forEach { entry ->
            renderEntry(context, entry)
        }
    }

    private fun renderEntry(
        context: LevelRenderContext,
        entry: StaticSourceIconState.Entry,
    ) {
        val stack = context.stack
        val cameraPosition = context.camera.position()

        stack.pushPose()

        // we have different hook points for forge and fabric
        // and for fabric for some reason we need to reset pose/normal to identity
        // on older versions to make everything work
        //#if MC<11800 && FABRIC
        //$$ stack.last().pose().setIdentity()
        //$$ stack.last().normal().setIdentity()
        //#endif

        stack.translate(
            entry.x - cameraPosition.x,
            entry.y - cameraPosition.y,
            entry.z - cameraPosition.z,
        )

        //#if MC>=1.21.11
        //$$ stack.mulPose(context.camera.orientation)
        //$$ stack.scale(0.025f, -0.025f, 0.025f)
        //#else
        stack.rotate(-context.camera.yRot, 0f, 1f, 0f)
        stack.rotate(context.camera.xRot, 1f, 0f, 0f)
        stack.scale(-0.025f, -0.025f, 0.025f)
        //#endif

        stack.translate(-5.0, -5.0, 0.0)

        //#if MC>=26.2
        //$$ val collector = context.submitNodeCollector
        //$$ vertices(collector, stack, 255, entry.light, entry, false)
        //$$ vertices(collector, stack, 40, entry.light, entry, true)
        //#else
        vertices(stack, 255, entry.light, entry, false)
        vertices(stack, 40, entry.light, entry, true)
        //#endif

        stack.popPose()
    }

    private fun vertices(
        //#if MC>=26.2
        //$$ collector: SubmitNodeCollector,
        //#endif
        stack: PoseStack,
        alpha: Int,
        light: Int,
        entry: StaticSourceIconState.Entry,
        seeThrough: Boolean,
    ) {
        //#if MC>=12111
        //$$ val renderType =
        //$$     if (seeThrough) RenderTypes.textSeeThrough(entry.iconLocation)
        //$$     else RenderTypes.text(entry.iconLocation)
        //#else
        val renderType =
            if (seeThrough) RenderType.textSeeThrough(entry.iconLocation)
            else RenderType.text(entry.iconLocation)
        //#endif

        //#if MC>=26.2
        //$$ val collection = collector.order(0) as SubmitNodeCollection
        //$$ collection.afterTerrain.submit(
        //$$     CustomFeatureRenderer.Submit(
        //$$         stack.last().copy(),
        //$$         renderType,
        //$$     ) { pose, buffer ->
        //$$         vertex(pose, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
        //$$         vertex(pose, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
        //$$         vertex(pose, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
        //$$         vertex(pose, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)
        //$$     }
        //$$ )
        //#else
        val renderPipeline = RenderPipelines.fromRenderType(
            if (seeThrough) "text_see_through" else "text",
            renderType,
        )

        val buffer = RenderUtil.beginBuffer(renderPipeline)

        vertex(stack, buffer, 0f, 10f, 0f, 0f, 1f, alpha, light)
        vertex(stack, buffer, 10f, 10f, 0f, 1f, 1f, alpha, light)
        vertex(stack, buffer, 10f, 0f, 0f, 1f, 0f, alpha, light)
        vertex(stack, buffer, 0f, 0f, 0f, 0f, 0f, alpha, light)

        //#if MC>=12100
        //$$ renderType.draw(buffer.buildOrThrow())
        //#elseif MC>=12000
        //$$ renderType.end(buffer, com.mojang.blaze3d.systems.RenderSystem.getVertexSorting())
        //#else
        renderType.end(buffer, 0, 0, 0)
        //#endif
        //#endif
    }

    private fun vertex(
        //#if MC>=26.2
        //$$ stack: PoseStack.Pose,
        //#else
        stack: PoseStack,
        //#endif
        buffer: VertexConsumer,
        x: Float, y: Float, z: Float,
        u: Float, v: Float,
        alpha: Int, light: Int,
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
