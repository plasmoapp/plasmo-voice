package su.plo.voice.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.connection.ServerInfo;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PushToTalkActivation extends BaseActivation {

    private final PlasmoVoiceClient voiceClient;
    private final KeyBinding keyBinding;
    private final KeyBinding priorityKeyBinding;

    public PushToTalkActivation(@NotNull PlasmoVoiceClient voiceClient,
                                @NotNull KeyBinding keyBinding,
                                @NotNull KeyBinding priorityKeyBinding) {
        this.voiceClient = voiceClient;
        this.keyBinding = checkNotNull(keyBinding, "keyBinding");
        this.priorityKeyBinding = checkNotNull(priorityKeyBinding, "priorityKeyBinding");
    }

    @Override
    public @NotNull Result process(short[] samples) {
        if (isDisabled() || !voiceClient.getCurrentServerInfo().isPresent())
            return Result.NOT_ACTIVATED;

        ServerInfo serverInfo = voiceClient.getCurrentServerInfo().get();

        boolean priorityPressed = priorityKeyBinding.isPressed()
                && serverInfo.getPlayerInfo().get("priority").orElse(0) == 1;

        boolean pressed = keyBinding.isPressed() || priorityPressed;

        if (priorityPressed && !activePriority) {
            this.activePriority = true;
        } else if (pressed && !priorityPressed && activePriority) {
            this.activePriority = false;
        }

        if (pressed) {
            if (!active) this.active = true;
            this.lastSpeak = System.currentTimeMillis();
        } else if (active && (System.currentTimeMillis() - lastSpeak > 350L)) {
            this.active = false;
            this.activePriority = false;

            return Result.END;
        }

        return active ? Result.ACTIVATED : Result.NOT_ACTIVATED;
    }

    @Override
    public @NotNull String getType() {
        return "PTT";
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.active = false;
        this.activePriority = false;
    }
}
