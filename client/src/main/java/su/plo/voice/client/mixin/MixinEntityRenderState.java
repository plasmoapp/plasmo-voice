package su.plo.voice.client.mixin;

//#if MC>=12102
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.Unique;
//$$ import su.plo.lib.mod.client.render.entity.LivingEntityRenderState;
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//$$
//$$ @Mixin(EntityRenderState.class)
//$$ public class MixinEntityRenderState implements EntityRenderStateAccessor {
//$$
//$$     @Unique
//$$     private LivingEntityRenderState livingEntityRenderState;
//$$
//$$     @Override
//$$     public LivingEntityRenderState plasmovoice_getLivingEntityRenderState() {
//$$         return livingEntityRenderState;
//$$     }
//$$
//$$     @Override
//$$     public void plasmovoice_setLivingEntityRenderState(LivingEntityRenderState livingEntityRenderState) {
//$$         this.livingEntityRenderState = livingEntityRenderState;
//$$     }
//$$ }
//#endif