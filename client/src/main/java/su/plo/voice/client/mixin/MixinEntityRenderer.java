package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.client.render.entity.LivingEntityRenderState;
import su.plo.voice.client.extension.EntityRendererKt;

//#if MC>=12102
//$$ import net.minecraft.client.renderer.entity.state.EntityRenderState;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//#endif

//#if MC>=12109
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//$$ import su.plo.lib.mod.client.render.entity.EntityRenderSubmitCollection;
//#else
import su.plo.voice.client.event.LivingEntityRenderEvent;
//#endif

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    //#if MC>=12102
    //$$ @Inject(method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    //$$ public void createRenderState(Entity entity, float f, CallbackInfoReturnable<EntityRenderState> cir) {
    //$$     if (!(entity instanceof LivingEntity)) return;
    //$$
    //$$     LocalPlayer clientPlayer = Minecraft.getInstance().player;
    //$$     if (clientPlayer == null) return;
    //$$
    //$$     LivingEntityRenderState entityRenderState = EntityRendererKt.createEntityRenderState(
    //$$             (EntityRenderer<?, ?>) ((Object) this),
    //$$             clientPlayer,
    //$$             (LivingEntity) entity
    //$$     );
    //$$
    //$$     EntityRenderStateAccessor entityRenderStateAccessor = (EntityRenderStateAccessor) cir.getReturnValue();
    //$$     entityRenderStateAccessor.plasmovoice_setLivingEntityRenderState(entityRenderState);
    //$$ }
    //$$

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
    //$$     LivingEntityRenderState livingEntityRenderState = entityRenderStateAccessor.plasmovoice_getLivingEntityRenderState();
    //$$
    //$$     if (livingEntityRenderState == null) return;
    //$$
    //$$     EntityRenderSubmitCollection.submit(livingEntityRenderState, poseStack, entityRenderState.lightCoords);
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
    //$$     LivingEntityRenderState livingEntityRenderState = entityRenderStateAccessor.plasmovoice_getLivingEntityRenderState();
    //$$
    //$$     if (livingEntityRenderState == null) return;
    //$$
    //$$     LivingEntityRenderEvent.INSTANCE.getInvoker().onRender(
    //$$             livingEntityRenderState,
    //$$             poseStack,
    //$$             light
    //$$     );
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

        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        LivingEntityRenderState entityRenderState = EntityRendererKt.createEntityRenderState(
                (EntityRenderer<?>) ((Object) this),
                clientPlayer,
                (LivingEntity) entity
        );

        LivingEntityRenderEvent.INSTANCE.getInvoker().onRender(
                entityRenderState,
                poseStack,
                light
        );
    }
    //#endif
}
