package su.plo.voice.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.world.entity.Display;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.lib.mod.client.render.entity.EntityRenderState;
import su.plo.voice.client.render.voice.EntityIconRenderer;
import su.plo.voice.client.render.voice.EntityVoiceIconState;

//#if MC>=1.21.2
//$$ import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
//$$ import su.plo.voice.client.render.EntityRenderStateAccessor;
//#else
import net.minecraft.client.renderer.entity.EntityRenderer;
import su.plo.lib.mod.client.render.entity.EntityRenderStateKt;
import su.plo.voice.client.render.voice.EntityIconStateExtractor;
//#endif

//#if MC>=1.21.9
//$$ import net.minecraft.client.renderer.SubmitNodeCollector;
//$$ import net.minecraft.client.renderer.state.CameraRenderState;
//#else
import net.minecraft.client.renderer.MultiBufferSource;
//#endif

@Mixin(DisplayRenderer.class)
public abstract class MixinDisplayRenderer {

    //#if MC>=1.21.2
    //$$ @Shadow
    //$$ private Quaternionf calculateOrientation(Display.RenderState renderState, DisplayEntityRenderState state, Quaternionf output) {
    //$$     throw new AssertionError();
    //$$ }
    //$$
    //#if MC>=1.21.9
    //$$ @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/DisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("RETURN"))
    //$$ public void submit(
    //$$         DisplayEntityRenderState displayState,
    //$$         PoseStack poseStack,
    //$$         SubmitNodeCollector submitNodeCollector,
    //$$         CameraRenderState cameraRenderState,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityVoiceIconState iconState = plasmovoice_getDisplayIconState(displayState);
    //$$     if (iconState == null) return;
    //$$
    //$$     EntityRenderState entityRenderState = new EntityRenderState(displayState);
    //$$     EntityIconRenderer.render(entityRenderState, iconState, cameraRenderState, submitNodeCollector, poseStack);
    //$$ }
    //#else
    //$$ @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/DisplayEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    //$$ public void render(
    //$$         DisplayEntityRenderState displayState,
    //$$         PoseStack poseStack,
    //$$         MultiBufferSource multiBufferSource,
    //$$         int light,
    //$$         CallbackInfo ci
    //$$ ) {
    //$$     EntityVoiceIconState iconState = plasmovoice_getDisplayIconState(displayState);
    //$$     if (iconState == null) return;
    //$$
    //$$     EntityRenderState entityRenderState = new EntityRenderState(displayState, light);
    //$$     EntityIconRenderer.render(entityRenderState, iconState, poseStack);
    //$$ }
    //#endif
    //$$
    //$$ @Unique
    //$$ private EntityVoiceIconState plasmovoice_getDisplayIconState(DisplayEntityRenderState displayState) {
    //$$     EntityVoiceIconState iconState = ((EntityRenderStateAccessor) displayState).plasmovoice_getEntityVoiceIconState();
    //$$     Display.RenderState renderState = displayState.renderState;
    //$$     if (iconState == null || renderState == null) return iconState;
    //$$
    //$$     Quaternionf orientation = calculateOrientation(renderState, displayState, new Quaternionf());
    //$$     return plasmovoice_offsetByTranslation(iconState, renderState.transformation().get(displayState.interpolationProgress), orientation);
    //$$ }
    //#else
    @Shadow
    private Quaternionf calculateOrientation(Display.RenderState renderState, Display display, float partialTick, Quaternionf output) {
        throw new AssertionError();
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Display;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    public void render(
            Display display,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource multiBufferSource,
            int light,
            CallbackInfo ci
    ) {
        EntityVoiceIconState iconState = EntityIconStateExtractor.extract(display);
        if (iconState == null) return;

        Display.RenderState renderState = display.renderState();
        if (renderState != null) {
            if (renderState.brightnessOverride() != -1) {
                light = renderState.brightnessOverride();
            }

            Quaternionf orientation = calculateOrientation(renderState, display, partialTick, new Quaternionf());
            Transformation transformation = renderState.transformation().get(display.calculateInterpolationProgress(partialTick));
            iconState = plasmovoice_offsetByTranslation(iconState, transformation, orientation);
        }

        EntityRenderState entityRenderState = EntityRenderStateKt.createEntityRenderState(
                (EntityRenderer<?>) ((Object) this),
                display,
                light
        );

        EntityIconRenderer.render(entityRenderState, iconState, poseStack);
    }
    //#endif

    @Unique
    private static EntityVoiceIconState plasmovoice_offsetByTranslation(
            EntityVoiceIconState iconState,
            Transformation transformation,
            Quaternionf orientation
    ) {
        Vector3f offset = orientation.transform(transformation.getTranslation(), new Vector3f());
        return iconState.withOffset(offset.x, offset.y, offset.z);
    }
}
