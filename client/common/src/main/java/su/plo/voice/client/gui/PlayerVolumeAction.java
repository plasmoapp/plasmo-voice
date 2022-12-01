package su.plo.voice.client.gui;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.entity.MinecraftPlayer;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.event.key.MouseScrollEvent;

import java.util.Optional;

public abstract class PlayerVolumeAction {

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;

    private MinecraftPlayer focusedPlayer;
    private long lastScroll;

    public PlayerVolumeAction(@NotNull MinecraftClientLib minecraft,
                              @NotNull PlasmoVoiceClient voiceClient,
                              @NotNull ClientConfig config) {
        this.minecraft = minecraft;
        this.voiceClient = voiceClient;
        this.config = config;

        voiceClient.getKeyBindings().getKeyBinding("key.plasmovoice.general.action")
                .ifPresent((key) -> key.addPressListener(this::onButton));
    }

    public boolean isShown(@NotNull MinecraftPlayer player) {
        return focusedPlayer != null &&
                focusedPlayer.getUUID().equals(player.getUUID()) &&
                lastScroll != 0L &&
                System.currentTimeMillis() - lastScroll < 1_000L;
    }

    @EventSubscribe
    public void onScroll(@NotNull MouseScrollEvent event) {
        if (focusedPlayer != null && !minecraft.getScreen().isPresent()) {
            this.lastScroll = System.currentTimeMillis();

            DoubleConfigEntry volume = config.getVoice().getVolumes().getVolume("source_" + focusedPlayer.getUUID());

            double value = volume.value() + (event.getVertical() > 0 ? 0.05D : -0.05D);
            volume.set((Math.round((value * volume.getMax() * 100D) / 5) * 5) / (volume.getMax() * 100D));

            event.setCancelled(true);
        }
    }

    private void onButton(@NotNull KeyBinding.Action action) {
        if (!voiceClient.getServerConnection().isPresent()) return;

        if (action == KeyBinding.Action.DOWN) {
            ServerConnection serverConnection = voiceClient.getServerConnection().get();

            getPlayerBySight()
                    .filter((player) -> serverConnection.getPlayerById(player.getUUID()).isPresent())
                    .ifPresent((player) -> this.focusedPlayer = player);
        } else if (action == KeyBinding.Action.UP) {
            this.focusedPlayer = null;
            this.lastScroll = 0L;
        }
    }

    protected abstract Optional<MinecraftPlayer> getPlayerBySight();
}
