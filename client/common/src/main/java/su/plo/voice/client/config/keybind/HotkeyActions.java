package su.plo.voice.client.config.keybind;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.client.config.ClientConfig;

@RequiredArgsConstructor
public final class HotkeyActions {

    private final KeyBindings keyBindings;
    private final ClientConfig config;

    public void register() {
        setHotkeyAction(
                "key.plasmovoice.general.mute_microphone",
                createConfigToggleAction(config.getVoice().getMicrophoneDisabled())
        );
        setHotkeyAction(
                "key.plasmovoice.general.disable_voice",
                createConfigToggleAction(config.getVoice().getDisabled())
        );
    }

    private KeyBinding.OnPress createConfigToggleAction(ConfigEntry<Boolean> entry) {
        return createKeyDownAction(() -> entry.set(!entry.value()));
    }

    private KeyBinding.OnPress createKeyDownAction(Runnable runnable) {
        return (action) -> {
            if (action != KeyBinding.Action.DOWN) return;
            runnable.run();
        };
    }

    private void setHotkeyAction(@NotNull String name, @NotNull KeyBinding.OnPress onPress) {
        keyBindings
                .getKeyBinding(name)
                .ifPresent(hotkey -> hotkey.onPress(onPress));
    }
}
