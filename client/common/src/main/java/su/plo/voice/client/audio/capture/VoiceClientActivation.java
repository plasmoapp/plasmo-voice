package su.plo.voice.client.audio.capture;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.config.entry.IntConfigEntry;
import su.plo.voice.proto.data.capture.Activation;
import su.plo.voice.proto.data.capture.VoiceActivation;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Config
public final class VoiceClientActivation extends VoiceActivation implements ClientActivation {

    private final ClientConfig config;

    private final IntConfigEntry configDistance;
    private final ConfigEntry<ClientActivation.Type> configType;
    private final ConfigEntry<Boolean> configToggle;

    private final KeyBindingConfigEntry pttKey;
    private final KeyBindingConfigEntry toggleKey;

    private final AtomicBoolean disabled = new AtomicBoolean(false);

    @Getter
    private boolean activated;
    @Getter
    private long lastActivation;

    public VoiceClientActivation(@NotNull ClientConfig config,
                                 @NotNull ConfigClientActivation activationConfig,
                                 @NotNull Activation activation) {
        super(
                activation.getName(),
                activation.getTranslation(),
                activation.getHudIconLocation(),
                activation.getSourceIconLocation(),
                new ArrayList<>(activation.getDistances()),
                activation.getDefaultDistance(),
                activation.getWeight()
        );

        this.config = config;
        ConfigKeyBindings keyBindings = config.getKeyBindings();

        // load values from config
        this.configDistance = activationConfig.getConfigDistance();
        this.configType = activationConfig.getConfigType();
        this.configToggle = activationConfig.getConfigToggle();

        // ptt
        String pttKeyName = "key.plasmovoice." + activation.getName() + ".ptt";
        Optional<KeyBindingConfigEntry> pttKey = keyBindings.getConfigKeyBinding(pttKeyName);
        if (!pttKey.isPresent()) {
            keyBindings.register(pttKeyName, ImmutableList.of(), "hidden", true);
            pttKey = keyBindings.getConfigKeyBinding(pttKeyName);
        }

        if (pttKey.isPresent()) this.pttKey = pttKey.get();
        else throw new IllegalStateException("Failed to register ptt keybinding");

        // toggle
        String toggleKeyname = "key.plasmovoice." + activation.getName() + ".toggle";
        Optional<KeyBindingConfigEntry> toggleKey = keyBindings.getConfigKeyBinding(toggleKeyname);
        if (!toggleKey.isPresent()) {
            keyBindings.register(toggleKeyname, ImmutableList.of(), "hidden", false);
            toggleKey = keyBindings.getConfigKeyBinding(toggleKeyname);
        }

        if (toggleKey.isPresent()) this.toggleKey = toggleKey.get();
        else throw new IllegalStateException("Failed to register toggle keybinding");

        toggleKey.get().value().onPress(this::onTogglePress);
    }

    @Override
    public Type getType() {
        return configType.value();
    }

    @Override
    public KeyBinding getPttKey() {
        return pttKey.value();
    }

    public KeyBindingConfigEntry getPttConfigEntry() {
        return pttKey;
    }

    @Override
    public KeyBinding getToggleKey() {
        return toggleKey.value();
    }

    public KeyBindingConfigEntry getToggleConfigEntry() {
        return toggleKey;
    }

    @Override
    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }

    @Override
    public boolean isDisabled() {
        return (getType() == Type.VOICE && configToggle.value()) || disabled.get();
    }

    @Override
    public int getDistance() {
        return configDistance.value();
    }

    @Override
    public @NotNull Result process(short[] samples) {
        if (isDisabled()) {
            if (this.activated) {
                this.activated = false;
                return Result.END;
            }

            return Result.NOT_ACTIVATED;
        }

        if (getType() == Type.PUSH_TO_TALK) {
            return handlePTT();
        } else if (getType() == Type.VOICE) {
            return handleVoice(samples);
        }

        return Result.NOT_ACTIVATED;
    }

    @Override
    public void reset() {
        this.activated = false;
        this.lastActivation = 0L;
    }

    private @NotNull Result handlePTT() {
        boolean pressed = getPttKey().isPressed();

        if (pressed) {
            if (!activated) this.activated = true;
            this.lastActivation = System.currentTimeMillis();
        } else if (activated && (System.currentTimeMillis() - lastActivation > 350L)) {
            this.activated = false;

            return Result.END;
        }

        return activated ? Result.ACTIVATED : Result.NOT_ACTIVATED;
    }

    private @NotNull Result handleVoice(short[] samples) {
        boolean lastActivated = System.currentTimeMillis() - lastActivation <= 500L;
        boolean voiceDetected = AudioUtil.containsMinAudioLevel(samples, config.getVoice().getActivationThreshold().value());
        if (lastActivated || voiceDetected) {
            if (voiceDetected) this.lastActivation = System.currentTimeMillis();
            if (!activated) this.activated = true;

            return Result.ACTIVATED;
        }

        if (activated) {
            this.activated = false;
            return Result.END;
        }

        return Result.NOT_ACTIVATED;
    }

    private void onTogglePress(@NotNull KeyBinding.Action action) {
        if (action != KeyBinding.Action.DOWN || getType() != Type.VOICE) return;
        configToggle.set(!configToggle.value());
    }
}
