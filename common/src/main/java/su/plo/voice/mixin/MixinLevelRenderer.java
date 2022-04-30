package su.plo.voice.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.render.SphereRenderer;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    private void onRenderWorldLastNormal(
            PoseStack matrices,
            float tickDelta, long limitTime, boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightmapTextureManager,
            Matrix4f matrix4f,
            CallbackInfo ci) {
        SphereRenderer.getInstance().render(matrices, matrix4f, minecraft);
    }

    @Inject(method = "renderLevel",
            slice = @Slice(from = @At(value = "FIELD", ordinal = 1,
                    target = "Lnet/minecraft/client/renderer/RenderStateShard;WEATHER_TARGET:Lnet/minecraft/client/renderer/RenderStateShard$OutputStateShard;"),
                    to = @At(value = "INVOKE", ordinal = 1,
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V")),
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/PostChain;process(F)V"))
    private void onRenderWorldLastFabulous(
            PoseStack matrices,
            float tickDelta, long limitTime, boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightTexture lightmapTextureManager,
            Matrix4f matrix4f,
            CallbackInfo ci) {
        SphereRenderer.getInstance().render(matrices, matrix4f, minecraft);
    }
}
