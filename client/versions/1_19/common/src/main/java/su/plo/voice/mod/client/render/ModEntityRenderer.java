package su.plo.voice.mod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.event.render.EntityRenderEvent;
import su.plo.lib.api.client.event.render.PlayerRenderEvent;
import su.plo.lib.mod.client.ModClientLib;
import su.plo.lib.mod.client.render.ModCamera;
import su.plo.lib.mod.entity.ModEntity;
import su.plo.lib.mod.entity.ModPlayer;
import su.plo.voice.api.client.PlasmoVoiceClient;

public final class ModEntityRenderer extends ModRenderer {

    private ModCamera camera;

    public ModEntityRenderer(@NotNull ModClientLib minecraft, @NotNull PlasmoVoiceClient voiceClient) {
        super(minecraft, voiceClient);
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

        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        setPoseStack(poseStack);
        setMultiBufferSource(multiBufferSource);

        voiceClient.getEventBus().call(new EntityRenderEvent(render, this.camera, light, new ModEntity<>(entity), hasLabel));
    }

    private void render(@NotNull PoseStack poseStack,
                        @NotNull MultiBufferSource multiBufferSource,
                        @NotNull Camera camera,
                        int light,
                        @NotNull AbstractClientPlayer player,
                        boolean hasLabel) {
        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        setPoseStack(poseStack);
        setMultiBufferSource(multiBufferSource);

        boolean isFakePlayer = false;
        if (Minecraft.getInstance().player != null) {
            isFakePlayer = !Minecraft.getInstance().getConnection().getOnlinePlayerIds().contains(player.getUUID());
        }

        voiceClient.getEventBus().call(new PlayerRenderEvent(
                render,
                this.camera,
                new ModPlayer<>(player),
                light,
                hasLabel,
                isFakePlayer
        ));
    }
}
