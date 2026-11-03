package su.plo.lib.mod.client.render.level

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.RenderType
import su.plo.lib.mod.client.render.GeometrySubmitContext

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.state.CameraRenderState
//#else
import net.minecraft.client.Camera
import net.minecraft.client.multiplayer.ClientLevel
//#endif

//#if MC>=26.2
//$$ import net.minecraft.client.renderer.SubmitNodeCollection
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.feature.CustomFeatureRenderer
//#else
import net.minecraft.client.Minecraft
//#endif

data class LevelRenderContext(
    //#if MC<1.21.11
    val level: ClientLevel,
    val camera: Camera,
    //#endif
    override val stack: PoseStack,
    val state: LevelRenderStateHolder,
    //#if MC>=26.2
    //$$ override val collector: SubmitNodeCollector,
    //#endif
) : GeometrySubmitContext {
    //#if MC>=1.21.11
    //$$ val camera: CameraRenderState
    //$$     get() = state.state.cameraRenderState
    //#endif

    override fun submitGeometry(renderType: RenderType, write: (PoseStack.Pose, VertexConsumer) -> Unit) {
        //#if MC>=26.2
        //$$ val collection = collector.order(0) as SubmitNodeCollection
        //$$ collection.afterTerrain.submit(
        //$$     CustomFeatureRenderer.Submit(stack.last().copy(), renderType) { pose, buffer -> write(pose, buffer) }
        //$$ )
        //#else
        val bufferSource = Minecraft.getInstance().renderBuffers().bufferSource()
        write(stack.last(), bufferSource.getBuffer(renderType))
        bufferSource.endBatch(renderType)
        //#endif
    }
}
