package su.plo.lib.mod.client.render.entity

//#if MC>=12109
//$$ import com.mojang.blaze3d.vertex.PoseStack
//$$
//$$ data class EntityRenderSubmit(
//$$     val entityRenderState: LivingEntityRenderState,
//$$     val stack: PoseStack,
//$$     val light: Int,
//$$ )
//$$
//$$ object EntityRenderSubmitCollection {
//$$
//$$     @JvmStatic
//$$     val submits = mutableListOf<EntityRenderSubmit>()
//$$
//$$ @JvmStatic
//$$ fun submit(
//$$     entityRenderState: LivingEntityRenderState,
//$$     stack: PoseStack,
//$$     light: Int,
//$$ ) {
//$$     submits.add(
//$$         EntityRenderSubmit(
//$$             entityRenderState,
//$$             PoseStack().also { it.last().set(stack.last()) },
//$$             light,
//$$         )
//$$     )
//$$ }
//$$
//$$     @JvmStatic
//$$     fun clear() {
//$$         submits.clear()
//$$     }
//$$ }
//#endif
