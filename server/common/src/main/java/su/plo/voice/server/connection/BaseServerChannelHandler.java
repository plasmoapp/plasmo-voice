package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.event.player.PlayerJoinEvent;
import su.plo.lib.api.server.event.player.PlayerQuitEvent;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.player.BaseVoicePlayer;
import su.plo.voice.server.util.version.ServerVersionUtil;
import su.plo.voice.util.version.SemanticVersion;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class BaseServerChannelHandler {

    protected final BaseVoiceServer voiceServer;

    protected final Map<UUID, PlayerChannelHandler> channels = Maps.newHashMap();

    private final Map<UUID, ScheduledFuture<?>> playerCheckFutures = Maps.newConcurrentMap();

    protected BaseServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        PlayerJoinEvent.INSTANCE.registerListener(this::onPlayerJoin);
        PlayerQuitEvent.INSTANCE.registerListener(this::onPlayerQuit);
    }

    public void clear() {
        channels.clear();

        PlayerJoinEvent.INSTANCE.unregisterListener(this::onPlayerJoin);
        PlayerQuitEvent.INSTANCE.unregisterListener(this::onPlayerQuit);
    }

    protected void handleRegisterChannels(List<String> channels, VoiceServerPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        if (channels.contains(BaseVoiceServer.CHANNEL_STRING)) {
            voiceServer.getTcpConnectionManager().requestPlayerInfo(player);
            cancelPlayerCheckFuture(player.getInstance().getUUID());

            ((BaseVoicePlayer<?>) player).setModLoader(
                    channels.contains("fml:handshake")
                            ? PlayerModLoader.FORGE
                            : PlayerModLoader.FABRIC
            );
        } else if (channels.contains("plasmo:voice")) {
            ((BaseVoicePlayer<?>) player).setModLoader(
                    channels.contains("fml:handshake")
                            ? PlayerModLoader.FORGE
                            : PlayerModLoader.FABRIC
            );

            ServerVersionUtil.suggestSupportedVersion(player,
                    SemanticVersion.parse(voiceServer.getVersion()),
                    voiceServer.getMinecraftServer().getVersion()
            );
        } else if (voiceServer.getConfig().voice().clientModRequired()) {
            kickModRequired(player.getInstance());
        }
    }

    public void onPlayerJoin(@NotNull MinecraftServerPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        if (voiceServer.getConfig().voice().clientModRequired()) {
            cancelPlayerCheckFuture(player.getUUID());

            playerCheckFutures.put(player.getUUID(), voiceServer.getBackgroundExecutor().schedule(() -> {
                voiceServer.getMinecraftServer().executeInMainThread(() -> kickModRequired(player));
            }, 3000L, TimeUnit.MILLISECONDS));
        }
    }

    public void onPlayerQuit(@NotNull MinecraftServerPlayer player) {
        channels.remove(player.getUUID());
        cancelPlayerCheckFuture(player.getUUID());
    }

    private void cancelPlayerCheckFuture(@NotNull UUID playerId) {
        ScheduledFuture<?> future = playerCheckFutures.remove(playerId);
        if (future != null) future.cancel(false);
    }

    private void kickModRequired(@NotNull MinecraftServerPlayer player) {
        player.kick(MinecraftTextComponent.translatable(
                "pv.error.mod_missing_kick_message"
        ));
    }
}
