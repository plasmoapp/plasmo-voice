package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.event.LivingEntityRenderEvent;
import su.plo.voice.client.mixin.accessor.EntityRendererAccessor;

//#if MC>=12102
//$$ import net.minecraft.client.Camera;
//$$ import net.minecraft.client.Minecraft;
//#else
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//#endif

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    //#if MC>=12102
    //$$ @Inject(
    //$$         method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
    //$$         at = @At(
    //$$                 value = "INVOKE",
    //$$                 shift = At.Shift.AFTER,
    //$$                 target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
    //$$         )
    //$$ )
    //$$ public void render(
    //$$         Entity entity,
    //$$         double d,
    //$$         double e,
    //$$         double f,
    //$$         float g,
    //$$         PoseStack poseStack,
    //$$         MultiBufferSource multiBufferSource,
    //$$         int light,
    //$$         EntityRenderer<?, ?> entityRenderer,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     if (!(entity instanceof LivingEntity)) return;
    //$$
    //$$     Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    //$$     double distanceToCamera = camera.getPosition().distanceToSqr(entity.position());
    //$$
    //$$     EntityRendererAccessor rendererAccessor = (EntityRendererAccessor) entityRenderer;
    //$$     LivingEntityRenderEvent.INSTANCE.getInvoker().onRender(
    //$$             (LivingEntity) entity,
    //$$             poseStack,
    //$$             light,
    //$$             rendererAccessor.shouldShowName(entity, distanceToCamera)
    //$$     );
    //$$ }
    //#else
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void render(
            Entity entity,
            double d,
            double e,
            double f,
            float g,
            float h,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            CallbackInfo ci,
            EntityRenderer<?> entityRenderer
    ) {
        if (!(entity instanceof LivingEntity)) return;

        EntityRendererAccessor rendererAccessor = (EntityRendererAccessor) entityRenderer;
        LivingEntityRenderEvent.INSTANCE.getInvoker().onRender(
                (LivingEntity) entity,
                poseStack,
                light,
                rendererAccessor.shouldShowName(entity)
        );
    }
    //#endif
}
