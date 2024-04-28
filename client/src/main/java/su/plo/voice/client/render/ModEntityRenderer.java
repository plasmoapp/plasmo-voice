package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.essential.universal.UMatrixStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.event.render.EntityRenderEvent;
import su.plo.voice.client.event.render.PlayerRenderEvent;

public final class ModEntityRenderer extends ModRenderer {

    public ModEntityRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        super(voiceClient);
    }

    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource multiBufferSource,
                       @NotNull Camera camera,
                       int light,
                       @NotNull Entity entity,
                       boolean hasLabel) {
        if (entity instanceof AbstractClientPlayer) {
            this.render(poseStack, multiBufferSource, camera, light, (AbstractClientPlayer) entity, hasLabel);
            return;
        }

        voiceClient.getEventBus().fire(new EntityRenderEvent(
                new UMatrixStack(poseStack),
                new ModCamera(camera.getPosition(), camera.getXRot(), camera.getYRot()),
                entity,
                light,
                hasLabel
        ));
    }

    private void render(@NotNull PoseStack poseStack,
                        @NotNull MultiBufferSource multiBufferSource,
                        @NotNull Camera camera,
                        int light,
                        @NotNull AbstractClientPlayer player,
                        boolean hasLabel) {
        boolean isFakePlayer = false;
        if (Minecraft.getInstance().player != null) {
            isFakePlayer = !Minecraft.getInstance().getConnection().getOnlinePlayerIds().contains(player.getUUID());
        }

        voiceClient.getEventBus().fire(new PlayerRenderEvent(
                new UMatrixStack(poseStack),
                new ModCamera(camera.getPosition(), camera.getXRot(), camera.getYRot()),
                player,
                light,
                hasLabel,
                isFakePlayer
        ));
    }
}
