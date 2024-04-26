package su.plo.voice.client.render.voice;

import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UResolution;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.config.IconPosition;
import su.plo.voice.api.client.event.render.HudActivationRenderEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.render.HudRenderEvent;

import java.util.List;

@RequiredArgsConstructor
public final class HudIconRenderer {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    @EventSubscribe
    public void onHudRender(@NotNull HudRenderEvent event) {
        if (!voiceClient.getServerInfo().isPresent() || !voiceClient.getUdpClientManager().getClient().isPresent() || UMinecraft.getPlayer() == null || !config.getOverlay().getShowActivationIcon().value())
            return;

        if (voiceClient.getUdpClientManager().getClient().get().isTimedOut()) {
            renderIcon(event.getStack(), new ResourceLocation("plasmovoice:textures/icons/microphone_disconnected.png"));
            return;
        }

        if (config.getVoice().getDisabled().value()) {
            renderIcon(event.getStack(), new ResourceLocation("plasmovoice:textures/icons/speaker_disabled.png"));
            return;
        }

        // server mute
        if (voiceClient.getAudioCapture().isServerMuted()) {
            renderIcon(event.getStack(), new ResourceLocation("plasmovoice:textures/icons/microphone_muted.png"));
            return;
        }

        if (config.getVoice().getMicrophoneDisabled().value()) {
            renderIcon(event.getStack(), new ResourceLocation("plasmovoice:textures/icons/microphone_disabled.png"));
            return;
        }

        List<ClientActivation> activations = (List<ClientActivation>) voiceClient.getActivationManager().getActivations();

        ClientActivation currentActivation = null;
        for (int index = activations.size() - 1; index >= 0; index--) {
            ClientActivation activation = activations.get(index);
            HudActivationRenderEvent renderEvent = new HudActivationRenderEvent(activation, activation.isActive());
            voiceClient.getEventBus().call(renderEvent);

            if (!renderEvent.isRender()) continue;

            currentActivation = activation;
            if (!activation.isTransitive()) break;
        }

        if (currentActivation != null) renderIcon(event.getStack(), new ResourceLocation(currentActivation.getIcon()));
    }

    private void renderIcon(@NotNull UMatrixStack stack, @NotNull ResourceLocation iconLocation) {
        IconPosition iconPosition = config.getOverlay().getActivationIconPosition().value();

        UGraphics.depthFunc(515);
        UGraphics.bindTexture(0, iconLocation);
        UGraphics.color4f(1F, 1F, 1F, 1F);

        stack.push();
        stack.translate(0f, 0f, 1000f);
        RenderUtil.blit(stack, calcIconX(iconPosition.getX()), calcIconY(iconPosition.getY()), 0, 0, 16, 16, 16, 16);
        stack.pop();
    }

    private int calcIconX(Integer x) {
        if (x == null) {
            return (UResolution.getScaledWidth() / 2) - 8;
        } else if (x < 0) {
            return UResolution.getScaledWidth() + x - 16;
        } else {
            return x;
        }
    }

    private int calcIconY(Integer y) {
        if (y == null) {
            return UResolution.getScaledHeight() - 32;
        } else if (y < 0) {
            return UResolution.getScaledHeight() + y - 16;
        } else {
            return y;
        }
    }
}
