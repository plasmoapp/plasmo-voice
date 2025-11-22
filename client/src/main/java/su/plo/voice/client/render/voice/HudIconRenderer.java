package su.plo.voice.client.render.voice;

import com.mojang.blaze3d.platform.Window;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.render.LazyGlState;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.config.IconPosition;
import su.plo.voice.api.client.event.render.HudActivationRenderEvent;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.event.HudRenderEvent;

import java.util.List;

@RequiredArgsConstructor
public final class HudIconRenderer  implements HudRenderEvent.Callback {

    private static final ResourceLocation MICROPHONE_DISCONNECTED_ICON =
            ResourceLocationUtil.parse("plasmovoice:textures/icons/microphone_disconnected.png");
    private static final ResourceLocation MICROPHONE_MUTED_ICON =
            ResourceLocationUtil.parse("plasmovoice:textures/icons/microphone_muted.png");
    private static final ResourceLocation MICROPHONE_DISABLED_ICON =
            ResourceLocationUtil.parse("plasmovoice:textures/icons/microphone_disabled.png");
    private static final ResourceLocation SPEAKER_DISABLED_ICON =
            ResourceLocationUtil.parse("plasmovoice:textures/icons/speaker_disabled.png");

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;

    // in most cases state should be the same, so let's hope it works
    private final @NotNull LazyGlState glState = new LazyGlState();

    @Override
    public void onRender(@NotNull GuiRenderContext context, float delta) {
        if (!voiceClient.getServerInfo().isPresent() ||
                !voiceClient.getUdpClientManager().getClient().isPresent() ||
                Minecraft.getInstance().player == null ||
                !config.getOverlay().getShowActivationIcon().value() ||
                Minecraft.getInstance().options.hideGui
        ) return;

        if (voiceClient.getUdpClientManager().getClient().get().isTimedOut()) {
            renderIcon(context, MICROPHONE_DISCONNECTED_ICON);
            return;
        }

        if (config.getVoice().getDisabled().value()) {
            renderIcon(context, SPEAKER_DISABLED_ICON);
            return;
        }

        // server mute
        if (voiceClient.getAudioCapture().isServerMuted()) {
            renderIcon(context, MICROPHONE_MUTED_ICON);
            return;
        }

        if (config.getVoice().getMicrophoneDisabled().value()) {
            renderIcon(context, MICROPHONE_DISABLED_ICON);
            return;
        }

        List<ClientActivation> activations = (List<ClientActivation>) voiceClient.getActivationManager().getActivations();

        ClientActivation currentActivation = null;
        for (int index = activations.size() - 1; index >= 0; index--) {
            ClientActivation activation = activations.get(index);
            HudActivationRenderEvent renderEvent = new HudActivationRenderEvent(activation, activation.isActive());
            voiceClient.getEventBus().fire(renderEvent);
            if (!renderEvent.isRender()) continue;

            currentActivation = activation;
            if (!activation.isTransitive()) break;
        }

        if (currentActivation != null) renderIcon(context, ResourceLocationUtil.parse(currentActivation.getIcon()));
    }

    private void renderIcon(@NotNull GuiRenderContext context, @NotNull ResourceLocation iconLocation) {
        IconPosition iconPosition = config.getOverlay().getActivationIconPosition().value();

        //#if MC<12106
        context.getStack().pushPose();
        context.getStack().translate(0f, 0f, 1000f);
        //#endif

        glState.withState(() ->
            context.blit(
                    iconLocation,
                    calcIconX(iconPosition.getX()),
                    calcIconY(iconPosition.getY()),
                    0,
                    0,
                    16,
                    16,
                    16,
                    16,
                    RenderPipelines.GUI_TEXTURE_OVERLAY
            )
        );
        //#if MC<12106
        context.getStack().popPose();
        //#endif
    }

    private int calcIconX(Integer x) {
        Window window = Minecraft.getInstance().getWindow();

        if (x == null) {
            return (window.getGuiScaledWidth() / 2) - 8;
        } else if (x < 0) {
            return window.getGuiScaledWidth() + x - 16;
        } else {
            return x;
        }
    }

    private int calcIconY(Integer y) {
        Window window = Minecraft.getInstance().getWindow();

        if (y == null) {
            return window.getGuiScaledHeight() - 32;
        } else if (y < 0) {
            return window.getGuiScaledHeight() + y - 16;
        } else {
            return y;
        }
    }
}
