package su.plo.voice.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.client.ModVoiceClient;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow protected abstract boolean shouldShowName(Entity par1);

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(LivingEntity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (ModVoiceClient.INSTANCE.getServerInfo().isEmpty()) return;

        ModVoiceClient.INSTANCE.getEntityRenderer().render(
                poseStack,
                Minecraft.getInstance().gameRenderer.getMainCamera(),
                i,
                entity,
                shouldShowName(entity)
        );
    }
}

