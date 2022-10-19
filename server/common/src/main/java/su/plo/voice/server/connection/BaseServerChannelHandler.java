package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.config.ServerLanguage;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;
import su.plo.voice.server.player.VoiceServerPlayer;

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

    protected void handleRegisterChannels(List<String> channels, VoicePlayer player) {
        if (!voiceServer.getUdpServer().isPresent()) return;

        if (channels.contains(BaseVoiceServer.CHANNEL_STRING)) {
            voiceServer.getTcpConnectionManager().connect(player);
            cancelPlayerCheckFuture(player.getInstance().getUUID());

            ((VoiceServerPlayer) player).setModLoader(
                    channels.contains("fml:handshake")
                            ? PlayerModLoader.FORGE
                            : PlayerModLoader.FABRIC
            );
        } else if (voiceServer.getConfig().getVoice().isClientModRequired()) {
            kickModRequired(player);
        }
    }

    @EventSubscribe
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (voiceServer.getConfig().getVoice().isClientModRequired()) {
            cancelPlayerCheckFuture(event.getPlayerId());

            playerCheckFutures.put(event.getPlayerId(), voiceServer.getExecutor().schedule(() -> {
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

    private void kickModRequired(VoicePlayer player) {
        ServerLanguage language = voiceServer.getLanguages().getLanguage(player.getInstance().getLanguage());
        player.getInstance().kick(MinecraftTextComponent.literal(
                language.modMissingKickMessage()
        ));
    }
}
