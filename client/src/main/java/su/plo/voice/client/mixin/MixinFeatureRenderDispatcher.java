package su.plo.voice.client.mixin;

//#if MC>=12109
//$$ import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import su.plo.lib.mod.client.render.entity.EntityRenderSubmitCollection;
//$$ import su.plo.voice.client.event.LivingEntityRenderEvent;
//$$
//$$ @Mixin(FeatureRenderDispatcher.class)
//$$ public class MixinFeatureRenderDispatcher {
//$$     @Inject(
//$$             method = "renderAllFeatures",
//$$             at = @At(
//$$                     value = "INVOKE",
//$$                     shift = At.Shift.AFTER,
//$$                     target = "Lnet/minecraft/client/renderer/feature/NameTagFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/gui/Font;)V"
//$$             )
//$$     )
//$$     public void renderAllFeatures(CallbackInfo ci) {
//$$         EntityRenderSubmitCollection.getSubmits().forEach(submit -> {
//$$             LivingEntityRenderEvent.INSTANCE.getInvoker().onRender(
//$$                     submit.getEntityRenderState(),
//$$                     submit.getStack(),
//$$                     submit.getLight()
//$$             );
//$$         });
//$$     }
//$$
//$$     @Inject(
//$$             method = "renderAllFeatures",
//$$             at = @At("TAIL")
//$$     )
//$$     public void renderAllFeaturesClear(CallbackInfo ci) {
//$$         EntityRenderSubmitCollection.clear();
//$$     }
//$$ }
//#endif
