package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.event.player.McPlayerJoinEvent;
import su.plo.slib.api.event.player.McPlayerQuitEvent;
import su.plo.slib.api.server.channel.McServerChannelHandler;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent;
import su.plo.voice.api.server.player.PlayerModLoader;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.player.BaseVoicePlayer;
import su.plo.voice.server.util.version.ServerVersionUtil;
import su.plo.voice.util.version.SemanticVersion;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ServerChannelHandler implements McServerChannelHandler {

    private final BaseVoiceServer voiceServer;

    private final Map<UUID, PlayerChannelHandler> channels = Maps.newHashMap();

    private final Map<UUID, ScheduledFuture<?>> playerCheckFutures = Maps.newConcurrentMap();

    public ServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        McPlayerJoinEvent.INSTANCE.registerListener(this::onPlayerJoin);
        McPlayerQuitEvent.INSTANCE.registerListener(this::onPlayerQuit);
        McPlayerRegisterChannelsEvent.INSTANCE.registerListener(this::handleRegisterChannels);
    }

    @Override
    public void receive(@NotNull McServerPlayer serverPlayer, @NotNull byte[] bytes) {
        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(bytes))
                    .ifPresent(packet -> {
                        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(serverPlayer.getInstance());

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                serverPlayer.getUuid(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        channel.handlePacket(packet);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        channels.clear();
    }

    private void handleRegisterChannels(@NotNull McServerPlayer serverPlayer, @NotNull List<String> channels) {
        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(serverPlayer.getInstance());

        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        ((BaseVoicePlayer<?>) voicePlayer).setModLoader(
                channels.contains("fml:handshake")
                        ? PlayerModLoader.FORGE
                        : PlayerModLoader.FABRIC
        );

        if (channels.contains(BaseVoiceServer.FLAG_CHANNEL_STRING)) {
            voiceServer.getTcpPacketManager().requestPlayerInfo(voicePlayer);
            cancelPlayerCheckFuture(serverPlayer.getUuid());
        } else if (channels.contains("plasmo:voice")) {
            ServerVersionUtil.suggestSupportedVersion(
                    voicePlayer,
                    SemanticVersion.parse(voiceServer.getVersion()),
                    voiceServer.getMinecraftServer().getVersion()
            );
        }
    }

    public void onPlayerJoin(@NotNull McPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        if (shouldKick(player)) {
            cancelPlayerCheckFuture(player.getUuid());

            playerCheckFutures.put(player.getUuid(), voiceServer.getBackgroundExecutor().schedule(() -> {
                voiceServer.getMinecraftServer().executeInMainThread(() -> kickModRequired(player));
            }, voiceServer.getConfig().voice().clientModRequiredCheckTimeoutMs(), TimeUnit.MILLISECONDS));
        }
    }

    public void onPlayerQuit(@NotNull McPlayer player) {
        channels.remove(player.getUuid());
        cancelPlayerCheckFuture(player.getUuid());
    }

    private boolean shouldKick(@NotNull McPlayer player) {
        return voiceServer.getConfig().voice().clientModRequired() &&
                !player.hasPermission("pv.bypass_mod_requirement");
    }

    private void cancelPlayerCheckFuture(@NotNull UUID playerId) {
        ScheduledFuture<?> future = playerCheckFutures.remove(playerId);
        if (future != null) future.cancel(false);
    }

    private void kickModRequired(@NotNull McPlayer player) {
        player.kick(McTextComponent.translatable("pv.error.mod_missing_kick_message"));
    }
}
