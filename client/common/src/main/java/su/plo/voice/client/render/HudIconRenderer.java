package su.plo.voice.client.render;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.event.render.HudRenderEvent;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.render.VertexBuilder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.IconPosition;

import java.util.List;

@RequiredArgsConstructor
public final class HudIconRenderer {

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    @EventSubscribe
    public void onHudRender(@NotNull HudRenderEvent event) {
        if (!voiceClient.getServerInfo().isPresent() ||
                !voiceClient.getUdpClientManager().getClient().isPresent() ||
                !minecraft.getClientPlayer().isPresent() ||
                !config.getOverlay().getShowActivationIcon().value()
        ) return;

        if (voiceClient.getUdpClientManager().getClient().get().isTimedOut()) {
            renderIcon(event.getRender(), "plasmovoice:textures/icons/microphone_disconnected.png");
            return;
        }

        if (config.getVoice().getDisabled().value()) {
            renderIcon(event.getRender(), "plasmovoice:textures/icons/speaker_disabled.png");
            return;
        }

        // server mute
        if (voiceClient.getAudioCapture().isServerMuted()) {
            renderIcon(event.getRender(), "plasmovoice:textures/icons/microphone_muted.png");
            return;
        }

        if (config.getVoice().getMicrophoneDisabled().value()) {
            renderIcon(event.getRender(), "plasmovoice:textures/icons/microphone_disabled.png");
            return;
        }

        List<ClientActivation> activations = (List<ClientActivation>) voiceClient.getActivationManager().getActivations();

        ClientActivation currentActivation = null;

        for (int index = activations.size() - 1; index >= 0; index--) {
            ClientActivation activation = activations.get(index);

            if (!activation.isActivated()) continue;

            currentActivation = activation;
            if (!activation.isTransitive()) break;
        }

        if (currentActivation != null)
            renderIcon(event.getRender(), currentActivation.getIcon());
    }

    private void renderIcon(@NotNull GuiRender render, @NotNull String iconLocation) {
        IconPosition iconPosition = config.getOverlay().getActivationIconPosition().value();

        render.setShader(VertexBuilder.Shader.POSITION_TEX);
        render.setShaderColor(1F, 1F, 1F, 1F);
        render.setShaderTexture(0, iconLocation);

        render.blit(
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
            return (minecraft.getWindow().getGuiScaledWidth() / 2) - 8;
        } else if (x < 0) {
            return minecraft.getWindow().getGuiScaledWidth() + x - 16;
        } else {
            return x;
        }
    }

    private int calcIconY(Integer y) {
        if (y == null) {
            return minecraft.getWindow().getGuiScaledHeight() - 32;
        } else if (y < 0) {
            return minecraft.getWindow().getGuiScaledHeight() + y - 16;
        } else {
            return y;
        }
    }
}
