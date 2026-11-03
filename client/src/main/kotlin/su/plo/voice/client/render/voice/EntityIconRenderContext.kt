package su.plo.voice.client.render.voice

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.render.GeometrySubmitContext
import su.plo.lib.mod.client.render.RenderUtil
import su.plo.slib.api.chat.component.McTextComponent

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//#else
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
//#endif

class EntityIconRenderContext(
    override val stack: PoseStack,
    //#if MC>=1.21.11
    //$$ val camera: CameraRenderState,
    //#if MC>=26.2
    //$$ override val collector: SubmitNodeCollector,
    //#else
    //$$ val collector: SubmitNodeCollector,
    //#endif
    //#else
    val camera: Camera,
    val bufferSource: MultiBufferSource,
    //#endif
) : GeometrySubmitContext {
    override fun submitGeometry(renderType: RenderType, write: (PoseStack.Pose, VertexConsumer) -> Unit) {
        //#if MC>=1.21.11
        //$$ collector.submitCustomGeometry(stack, renderType) { pose, buffer -> write(pose, buffer) }
        //#else
        write(stack.last(), bufferSource.getBuffer(renderType))
        //#endif
    }
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
    val charSequence = RenderUtil.getVisualOrderText(text)
    val displayMode = if (seeThrough) Font.DisplayMode.SEE_THROUGH else Font.DisplayMode.NORMAL

    //#if MC>=1.21.11
    //$$ collector.submitText(
    //$$     stack,
    //$$     x.toFloat(), y.toFloat(),
    //$$     charSequence,
    //$$     false,
    //$$     displayMode,
    //$$     light,
    //$$     color,
    //$$     backgroundColor,
    //$$     0,
    //$$ )
    //#else
    Minecraft.getInstance().font.drawInBatch(
        charSequence,
        x.toFloat(), y.toFloat(),
        color,
        false,
        stack.last().pose(),
        bufferSource,
        displayMode,
        backgroundColor,
        light,
    )
    //#endif
}
