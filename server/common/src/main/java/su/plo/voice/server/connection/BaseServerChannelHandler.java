package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.event.player.PlayerJoinEvent;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
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
    }

    public void clear() {
        channels.clear();
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
            kickModRequired(player);
        }
    }

    @EventSubscribe
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        if (voiceServer.getConfig().voice().clientModRequired()) {
            cancelPlayerCheckFuture(event.getPlayerId());

            playerCheckFutures.put(event.getPlayerId(), voiceServer.getBackgroundExecutor().schedule(() -> {
                voiceServer.getPlayerManager().getPlayerById(event.getPlayerId()).ifPresent((player) ->
                        voiceServer.getMinecraftServer().executeInMainThread(() -> kickModRequired(player))
                );
            }, 3000L, TimeUnit.MILLISECONDS));
        }
    }

    @EventSubscribe
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        channels.remove(event.getPlayerId());

        cancelPlayerCheckFuture(event.getPlayerId());
    }

    private void cancelPlayerCheckFuture(@NotNull UUID playerId) {
        ScheduledFuture<?> future = playerCheckFutures.remove(playerId);
        if (future != null) future.cancel(false);
    }

    private void kickModRequired(VoiceServerPlayer player) {
        player.getInstance().kick(MinecraftTextComponent.translatable(
                "pv.error.mod_missing_kick_message"
        ));
    }
}
