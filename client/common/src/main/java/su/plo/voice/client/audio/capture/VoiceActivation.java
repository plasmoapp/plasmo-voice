package su.plo.voice.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.util.AudioUtil;
import su.plo.voice.client.config.ClientConfig;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceActivation extends BaseActivation {

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig.Voice voiceConfig;
    private final KeyBinding priorityKeyBinding;

    public VoiceActivation(@NotNull PlasmoVoiceClient voiceClient,
                           @NotNull ClientConfig.Voice voiceConfig,
                           @NotNull KeyBinding priorityKeyBinding) {
        this.voiceClient = voiceClient;
        this.voiceConfig = voiceConfig;
        this.priorityKeyBinding = checkNotNull(priorityKeyBinding, "priorityKeyBinding");
    }

    @Override
    public @NotNull Result process(short[] samples) {
        if (isDisabled() || !voiceClient.getCurrentServerInfo().isPresent())
            return Result.NOT_ACTIVATED;

        ServerInfo serverInfo = voiceClient.getCurrentServerInfo().get();

        boolean priorityPressed = priorityKeyBinding.isPressed()
                && serverInfo.getPlayerInfo().get("priority").orElse(0) == 1;

        if (priorityPressed && !activePriority) this.activePriority = true;
        if (priorityPressed) {
            if (!active) this.active = true;
            this.lastSpeak = System.currentTimeMillis();
            return Result.ACTIVATED;
        }

        if (activePriority) this.activePriority = false; // todo: check new priority activation behavior in voice activation

        boolean lastActivated = System.currentTimeMillis() - lastSpeak <= 500L;
        boolean voiceDetected = AudioUtil.containsMinAudioLevel(samples, voiceConfig.getVoiceActivationThreshold().value());
        if (lastActivated || voiceDetected) {
            if (voiceDetected) this.lastSpeak = System.currentTimeMillis();
            if (!active) this.active = true;

            return Result.ACTIVATED;
        }

        if (active) {
            this.active = false;
            return Result.END;
        }

        return Result.NOT_ACTIVATED;
    }

    @Override
    public @NotNull String getType() {
        return "VOICE";
    }
}
