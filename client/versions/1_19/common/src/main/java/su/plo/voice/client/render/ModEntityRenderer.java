package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.event.render.EntityRenderEvent;
import su.plo.lib.client.event.render.PlayerRenderEvent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.entity.ModEntity;
import su.plo.voice.client.entity.ModPlayer;
import su.plo.voice.lib.client.ModClientLib;
import su.plo.voice.lib.client.render.ModCamera;

public final class ModEntityRenderer extends ModRenderer {

    private ModCamera camera;

    public ModEntityRenderer(@NotNull ModClientLib minecraft, @NotNull PlasmoVoiceClient voiceClient) {
        super(minecraft, voiceClient);
    }

    public void render(@NotNull PoseStack poseStack, @NotNull Camera camera, @NotNull AbstractClientPlayer player, boolean hasLabel) {
        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        setPoseStack(poseStack);

        boolean isFakePlayer = false;
        if (Minecraft.getInstance().player != null) {
            isFakePlayer = !Minecraft.getInstance().getConnection().getOnlinePlayerIds().contains(player.getUUID());
        }

        voiceClient.getEventBus().call(new PlayerRenderEvent(
                render,
                this.camera,
                new ModPlayer(player),
                hasLabel,
                isFakePlayer
        ));
    }

    public void render(@NotNull PoseStack poseStack, @NotNull Camera camera, @NotNull Entity entity) {
        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        setPoseStack(poseStack);

        voiceClient.getEventBus().call(new EntityRenderEvent(render, this.camera, new ModEntity(entity)));
    }
}
