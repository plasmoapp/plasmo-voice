package su.plo.voice.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.render.SourceIconRenderer;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    private void onRenderWorldLastNormal(
            PoseStack poseStack,
            float tickDelta, long limitTime, boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightmapTextureManager,
            Matrix4f matrix4f,
            CallbackInfo ci) {
        SourceIconRenderer.getInstance().renderStatics(poseStack, camera, matrix4f);
    }

    @Inject(method = "renderLevel",
            slice = @Slice(from = @At(value = "FIELD", ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/RenderStateShard;WEATHER_TARGET:Lnet/minecraft/client/renderer/RenderStateShard$OutputStateShard;"),
                    to = @At(value = "INVOKE", ordinal = 1,
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V")),
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"))
    private void onRenderWorldLastFabulous(
            PoseStack poseStack,
            float tickDelta, long limitTime, boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightmapTextureManager,
            Matrix4f matrix4f,
            CallbackInfo ci) {
        SourceIconRenderer.getInstance().renderStatics(poseStack, camera, matrix4f);
    }
}
