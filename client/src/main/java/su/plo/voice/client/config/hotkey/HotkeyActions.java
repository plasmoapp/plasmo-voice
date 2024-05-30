package su.plo.voice.client.config.hotkey;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.mod.client.chat.ClientChatUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.config.hotkey.Hotkeys;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerStatePacket;

@RequiredArgsConstructor
public final class HotkeyActions {

    private final PlasmoVoiceClient voiceClient;
    private final Hotkeys hotkeys;
    private final VoiceClientConfig config;

    public void register() {
        setHotkeyAction(
                "key.plasmovoice.general.mute_microphone",
                createConfigToggleAction(config.getVoice().getMicrophoneDisabled())
        );
        config.getVoice().getMicrophoneDisabled().addChangeListener((value) -> sendPlayerStatePacket());

        setHotkeyAction(
                "key.plasmovoice.general.disable_voice",
                createConfigToggleAction(config.getVoice().getDisabled())
        );
        config.getVoice().getDisabled().addChangeListener((value) -> sendPlayerStatePacket());

        setHotkeyAction(
                "key.plasmovoice.occlusion.toggle",
                createKeyDownAction(() -> {
                    ConfigEntry<Boolean> entry = config.getVoice().getSoundOcclusion();
                    entry.set(!entry.value());

                    ClientChatUtil.setActionBar(
                            McTextComponent.translatable(
                                    "message.plasmovoice.occlusion_changed",
                                    entry.value()
                                            ? McTextComponent.translatable("message.plasmovoice.on")
                                            : McTextComponent.translatable("message.plasmovoice.off")
                            )
                    );
                })
        );
    }

    private Hotkey.OnPress createConfigToggleAction(ConfigEntry<Boolean> entry) {
        return createKeyDownAction(() -> entry.set(!entry.value()));
    }

    private Hotkey.OnPress createKeyDownAction(Runnable runnable) {
        return (action) -> {
            if (action != Hotkey.Action.DOWN) return;
            runnable.run();
        };
    }

    private void setHotkeyAction(@NotNull String name, @NotNull Hotkey.OnPress onPress) {
        hotkeys
                .getHotkey(name)
                .ifPresent(hotkey -> hotkey.addPressListener(onPress));
    }

    private void sendPlayerStatePacket() {
        voiceClient.getServerConnection()
                .ifPresent((connection) ->
                        connection.sendPacket(new PlayerStatePacket(
                                config.getVoice().getDisabled().value(),
                                config.getVoice().getMicrophoneDisabled().value()
                        ))
                );
    }
}
