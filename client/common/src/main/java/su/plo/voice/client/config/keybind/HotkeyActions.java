package su.plo.voice.client.config.keybind;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.config.ClientConfig;

@RequiredArgsConstructor
public final class HotkeyActions {

    private final MinecraftClientLib minecraft;
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

        setHotkeyAction(
                "key.plasmovoice.occlusion.toggle",
                createKeyDownAction(() -> {
                    ConfigEntry<Boolean> entry = config.getVoice().getSoundOcclusion();
                    entry.set(!entry.value());

                    minecraft.getClientPlayer()
                            .ifPresent(player -> { // lambdas hell
                                player.sendActionbarMessage(TextComponent.translatable(
                                        "message.plasmovoice.occlusion_changed",
                                        entry.value()
                                                ? TextComponent.translatable("message.plasmovoice.on")
                                                : TextComponent.translatable("message.plasmovoice.off")
                                ));
                            });
                })
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
