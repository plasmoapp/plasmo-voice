package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState;
import su.plo.voice.client.render.voice.EntityIconStateExtractor;
import su.plo.voice.client.render.voice.EntityIconRenderer;
import su.plo.voice.client.render.voice.EntityVoiceIconState;

//#if MC>=12102
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#else
import su.plo.lib.mod.client.render.entity.LivingEntityRenderStateKt;
//#endif

//#if MC>=12102
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//#endif

//#if MC>=12109
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//#endif

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    //#if MC>=12102
    //$$ @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    //$$ public void createRenderState(Entity entity, float f, CallbackInfoReturnable<EntityRenderState> cir) {
    //$$     if (!(entity instanceof LivingEntity)) return;
    //$$
    //$$     EntityVoiceIconState voiceIconState = EntityIconStateExtractor.extract((LivingEntity) entity);
    //$$
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) cir.getReturnValue();
    //$$     entityRenderStateAccessor.plasmovoice_setEntityVoiceIcon(voiceIconState);
    //$$ }

    //#if MC>=12109
    //$$ @Inject(method = "submit", at = @At("RETURN"))
    //$$ public void submit(
    //$$         EntityRenderState entityRenderState,
    //$$         PoseStack poseStack,
    //$$         SubmitNodeCollector submitNodeCollector,
    //$$         CameraRenderState cameraRenderState,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) entityRenderState;
    //$$     EntityVoiceIconState voiceIconState = entityRenderStateAccessor.plasmovoice_getEntityVoiceIconState();
    //$$
    //$$     if (voiceIconState == null) return;
    //$$
    //$$     LivingEntityRenderState livingEntityRenderState = new LivingEntityRenderState(entityRenderState);
    //$$
    //$$     EntityIconRenderer.render(livingEntityRenderState, voiceIconState, cameraRenderState, submitNodeCollector, poseStack);
    //$$ }
    //#else
    //$$ @Inject(method = "render", at = @At("RETURN"))
    //$$ public void render(
    //$$         EntityRenderState entityRenderState,
    //$$         PoseStack poseStack,
    //$$         MultiBufferSource multiBufferSource,
    //$$         int light,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) entityRenderState;
    //$$     EntityVoiceIconState voiceIconState = entityRenderStateAccessor.plasmovoice_getEntityVoiceIconState();
    //$$
    //$$     if (voiceIconState == null) return;
    //$$
    //$$     LivingEntityRenderState livingEntityRenderState = new LivingEntityRenderState(entityRenderState, light);
    //$$
    //$$     EntityIconRenderer.render(livingEntityRenderState, voiceIconState, poseStack);
    //$$ }
    //#endif

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
        if (!(entity instanceof LivingEntity)) return;

        EntityVoiceIconState iconState = EntityIconStateExtractor.extract((LivingEntity) entity);
        if (iconState == null) return;

        LivingEntityRenderState entityRenderState = LivingEntityRenderStateKt.createEntityRenderState(
                (EntityRenderer<?>) ((Object) this),
                (LivingEntity) entity,
                light
        );

        EntityIconRenderer.render(entityRenderState, iconState, poseStack);
    }
    //#endif
}
