package su.plo.voice.client.render.voice

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.resources.ResourceLocation
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent

//#if MC>=1.21.11
//$$ import net.minecraft.client.gui.Font
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//#else
import net.minecraft.client.Camera
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.render.pipeline.RenderPipelines
//#endif

//#if MC>=26.3
//$$ import su.plo.lib.mod.client.render.feature.SeeThroughGeometryFeatureRenderer
//#endif

class EntityIconRenderContext(
    val stack: PoseStack,
    //#if MC>=1.21.11
    //$$ val camera: CameraRenderState,
    //$$ val collector: SubmitNodeCollector,
    //#else
    val camera: Camera,
    //#endif
)

fun EntityIconRenderContext.submitTexturedGeometry(
    texture: ResourceLocation,
    seeThrough: Boolean,
    write: (PoseStack.Pose, VertexConsumer) -> Unit,
) {
    //#if MC>=1.21.11
    //$$ val renderType = if (seeThrough) RenderTypes.textSeeThrough(texture) else RenderTypes.text(texture)
    //$$ val geometry = SubmitNodeCollector.CustomGeometryRenderer { pose, buffer -> write(pose, buffer) }
    //$$
    //#if MC>=26.3
    //$$ if (seeThrough) {
    //$$     SeeThroughGeometryFeatureRenderer.submit(collector, stack.last().copy(), renderType, geometry)
    //$$ } else {
    //$$     collector.submitCustomGeometry(stack, renderType, geometry)
    //$$ }
    //#else
    //$$ collector.submitCustomGeometry(stack, renderType, geometry)
    //#endif
    //#else
    val renderType = if (seeThrough) RenderType.textSeeThrough(texture) else RenderType.text(texture)
    val pipeline = RenderPipelines.fromRenderType(if (seeThrough) "text_see_through" else "text", renderType)

    val buffer = RenderUtil.beginBuffer(pipeline)
    write(stack.last(), buffer)
    renderType.draw(buffer.build())
    //#endif
}

fun EntityIconRenderContext.submitText(
    text: McTextComponent,
    x: Int,
    y: Int,
    color: Int,
    backgroundColor: Int,
    light: Int,
    seeThrough: Boolean,
) {
    //#if MC>=1.21.11
    //$$ collector.submitText(
    //$$     stack,
    //$$     x.toFloat(), y.toFloat(),
    //$$     RenderUtil.getVisualOrderText(text),
    //$$     false,
    //$$     if (seeThrough) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL,
    //$$     light,
    //$$     color,
    //$$     backgroundColor,
    //$$     0,
    //$$ )
    //#else
    RenderUtil.drawStringLight(stack, text, x, y, color, backgroundColor, light, seeThrough, false)
    //#endif
}
