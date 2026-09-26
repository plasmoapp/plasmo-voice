package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.client.render.entity.EntityRenderState;
import su.plo.voice.client.render.voice.EntityIconStateExtractor;
import su.plo.voice.client.render.voice.EntityIconRenderer;
import su.plo.voice.client.render.voice.EntityVoiceIconState;

//#if MC>=12102
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#else
import su.plo.lib.mod.client.render.entity.EntityRenderStateKt;
//#endif

//#if MC>=12102
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//#endif

//#if MC>=12109
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//#else
import net.minecraft.client.renderer.MultiBufferSource;
//#endif

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    //#if MC>=12102
    //$$ @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    //$$ public void createRenderState(Entity entity, float f, CallbackInfoReturnable<net.minecraft.client.renderer.entity.state.EntityRenderState> cir) {
    //$$     EntityVoiceIconState voiceIconState = EntityIconStateExtractor.extract(entity);
    //$$
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) cir.getReturnValue();
    //$$     entityRenderStateAccessor.plasmovoice_setEntityVoiceIcon(voiceIconState);
    //$$ }
    //$$
    //#if MC>=12109
    //$$ @Inject(method = "submit", at = @At("RETURN"))
    //$$ public void submit(
    //$$         net.minecraft.client.renderer.entity.state.EntityRenderState mcEntityRenderState,
    //$$         PoseStack poseStack,
    //$$         SubmitNodeCollector submitNodeCollector,
    //$$         CameraRenderState cameraRenderState,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) mcEntityRenderState;
    //$$     EntityVoiceIconState voiceIconState = entityRenderStateAccessor.plasmovoice_getEntityVoiceIconState();
    //$$
    //$$     if (voiceIconState == null) return;
    //$$
    //$$     EntityRenderState entityRenderState = new EntityRenderState(mcEntityRenderState);
    //$$
    //$$     EntityIconRenderer.render(entityRenderState, voiceIconState, cameraRenderState, submitNodeCollector, poseStack);
    //$$ }
    //#else
    //$$ @Inject(method = "render", at = @At("RETURN"))
    //$$ public void render(
    //$$         net.minecraft.client.renderer.entity.state.EntityRenderState mcEntityRenderState,
    //$$         PoseStack poseStack,
    //$$         MultiBufferSource multiBufferSource,
    //$$         int light,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) mcEntityRenderState;
    //$$     EntityVoiceIconState voiceIconState = entityRenderStateAccessor.plasmovoice_getEntityVoiceIconState();
    //$$
    //$$     if (voiceIconState == null) return;
    //$$
    //$$     EntityRenderState entityRenderState = new EntityRenderState(mcEntityRenderState, light);
    //$$
    //$$     EntityIconRenderer.render(entityRenderState, voiceIconState, poseStack);
    //$$ }
    //#endif
    //$$
    //#else
    @Inject(method = "render", at = @At("RETURN"))
    public void render(
            Entity entity,
            float f,
            float g,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            CallbackInfo ci
    ) {
        EntityVoiceIconState iconState = EntityIconStateExtractor.extract(entity);
        if (iconState == null) return;

        EntityRenderState entityRenderState = EntityRenderStateKt.createEntityRenderState(
                (EntityRenderer<?>) ((Object) this),
                entity,
                light
        );

        EntityIconRenderer.render(entityRenderState, iconState, poseStack);
    }
    //#endif
}
