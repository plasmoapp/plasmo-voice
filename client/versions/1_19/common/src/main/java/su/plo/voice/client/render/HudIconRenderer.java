package su.plo.voice.client.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.Map;

@RequiredArgsConstructor
public final class HudIconRenderer {

    private final Minecraft client = Minecraft.getInstance();
    // cache resource locations to avoid unnecessary allocations on render
    private final Map<String, ResourceLocation> cachedIconLocations = Maps.newHashMap();
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    public void render() {
        if (voiceClient.getServerInfo().isEmpty()) return;

        Player player = client.player;
        if (player == null) return;

        // todo: timed out

        voiceClient.getAudioCapture()
                .getActivationById(VoiceActivation.PROXIMITY_ID)
                .ifPresent(activation -> {
                    if (activation.isActivated()) {
                        renderIcon(getActivationIconLocation(activation));
                    }
                });

        for (ClientActivation activation : voiceClient.getAudioCapture().getActivations()) {
            if (!activation.isActivated()) continue;

            renderIcon(getActivationIconLocation(activation));

            if (!activation.isTransitive()) break;
        }
    }

    private void renderIcon(@NotNull ResourceLocation iconLocation) {
        ClientConfig.UI.ActivationIconPosition iconPosition = config.getUi().getActivationIconPosition().value();

        PoseStack poseStack = new PoseStack();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, iconLocation);

        GuiComponent.blit(
                poseStack,
                calcIconX(iconPosition.getX()),
                calcIconY(iconPosition.getY()),
                0,
                0,
                16,
                16,
                16,
                16
        );
    }

    private int calcIconX(Integer x) {
        if (x == null) {
            return (client.getWindow().getGuiScaledWidth() / 2) - 8;
        } else if (x < 0) {
            return client.getWindow().getGuiScaledWidth() + x - 16;
        } else {
            return x;
        }
    }

    private int calcIconY(Integer y) {
        if (y == null) {
            return client.getWindow().getGuiScaledHeight() - 32;
        } else if (y < 0) {
            return client.getWindow().getGuiScaledHeight() + y - 16;
        } else {
            return y;
        }
    }

    private ResourceLocation getActivationIconLocation(ClientActivation activation) {
        return cachedIconLocations.computeIfAbsent(
                activation.getHudIconLocation(),
                ResourceLocation::new
        );
    }
}
