package su.plo.lib.mod.client.render.feature

//#if MC>=26.3
//$$ import com.mojang.blaze3d.vertex.PoseStack
//$$ import net.minecraft.client.renderer.SubmitNodeCollector
//$$ import net.minecraft.client.renderer.feature.FeatureFrameContext
//$$ import net.minecraft.client.renderer.feature.FeatureRendererType
//$$ import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer
//$$ import net.minecraft.client.renderer.feature.submit.TranslucentSubmit
//$$ import net.minecraft.client.renderer.rendertype.RenderType
//$$
//#if FABRIC
//$$ import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases
//#else
//$$ import net.neoforged.neoforge.client.submit.RenderPhaseKey
//#endif
//$$
//$$ class SeeThroughGeometryFeatureRenderer : RenderTypeFeatureRenderer<SeeThroughGeometryFeatureRenderer.Submit>() {
//$$     override fun buildGroup(context: FeatureFrameContext, submits: List<Submit>) {
//$$         for (submit in submits) {
//$$             submit.renderer.render(submit.pose, getVertexBuilder(submit.renderType))
//$$         }
//$$     }
//$$
//$$     class Submit(
//$$         val pose: PoseStack.Pose,
//$$         val renderType: RenderType,
//$$         val renderer: SubmitNodeCollector.CustomGeometryRenderer,
//$$     ) : TranslucentSubmit {
//$$         override fun distanceToCameraSq(): Float =
//$$             TranslucentSubmit.computeDistanceToCameraSq(pose.pose())
//$$
//$$         override fun featureType(): FeatureRendererType<Submit> = TYPE
//$$     }
//$$
//$$     companion object {
//$$         @JvmField
//$$         val TYPE: FeatureRendererType<Submit> = FeatureRendererType.create("PlasmoVoiceSeeThroughGeometry")
//$$
//$$         @JvmStatic
//$$         fun submit(
//$$             collector: SubmitNodeCollector,
//$$             pose: PoseStack.Pose,
//$$             renderType: RenderType,
//$$             renderer: SubmitNodeCollector.CustomGeometryRenderer,
//$$         ) {
//#if FABRIC
//$$             collector.submitCustom(SubmitRenderPhases.SEE_THROUGH_NAME_TAGS, Submit(pose, renderType, renderer))
//#else
//$$             collector.submitSpecial(SEE_THROUGH, Submit(pose, renderType, renderer))
//#endif
//$$         }
//#if NEOFORGE
//$$
//$$         private val SEE_THROUGH = RenderPhaseKey { it.seeThrough }
//#endif
//$$     }
//$$ }
//#endif
