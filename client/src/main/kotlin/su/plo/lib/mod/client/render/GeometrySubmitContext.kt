package su.plo.lib.mod.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

//#if MC>=1.21.11
//$$ import net.minecraft.client.renderer.rendertype.RenderTypes
//#endif

//#if MC>=26.2
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//#endif

//#if MC>=26.3
//$$ import su.plo.lib.mod.client.render.feature.SeeThroughGeometryFeatureRenderer
//#endif

interface GeometrySubmitContext {
    val stack: PoseStack

    //#if MC>=26.2
    //$$ val collector: SubmitNodeCollector
    //#endif

    fun submitGeometry(renderType: RenderType, write: (PoseStack.Pose, VertexConsumer) -> Unit)
}

fun GeometrySubmitContext.submitTexturedGeometry(
    texture: ResourceLocation,
    seeThrough: Boolean,
    write: (PoseStack.Pose, VertexConsumer) -> Unit,
) {
    //#if MC>=1.21.11
    //$$ val renderType = if (seeThrough) RenderTypes.textSeeThrough(texture) else RenderTypes.text(texture)
    //#else
    val renderType = if (seeThrough) RenderType.textSeeThrough(texture) else RenderType.text(texture)
    //#endif

    //#if MC>=26.3
    //$$ if (seeThrough) {
    //$$     SeeThroughGeometryFeatureRenderer.submit(collector, stack.last().copy(), renderType) { pose, buffer ->
    //$$         write(pose, buffer)
    //$$     }
    //$$     return
    //$$ }
    //#endif

    submitGeometry(renderType, write)
}
