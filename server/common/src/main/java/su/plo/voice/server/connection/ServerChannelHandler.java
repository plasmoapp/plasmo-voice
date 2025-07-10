package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.event.player.McPlayerJoinEvent;
import su.plo.slib.api.event.player.McPlayerQuitEvent;
import su.plo.slib.api.server.channel.McServerChannelHandler;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ServerChannelHandler implements McServerChannelHandler {

    private final BaseVoiceServer voiceServer;

    private final Map<UUID, PlayerChannelHandler> channels = Maps.newConcurrentMap();

    private final Map<UUID, ScheduledFuture<?>> playerCheckFutures = Maps.newConcurrentMap();
    private final Set<UUID> joinPacketSent = Sets.newConcurrentHashSet();
    private final Set<UUID> channelRegisterPacketSent = Sets.newConcurrentHashSet();

    public ServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        McPlayerJoinEvent.INSTANCE.registerListener(this::onPlayerJoin);
        McPlayerQuitEvent.INSTANCE.registerListener(this::onPlayerQuit);

        McPlayerRegisterChannelsEvent.INSTANCE.registerListener(this::onChannelsRegister);
    }

    @Override
    public void receive(@NotNull McServerPlayer serverPlayer, @NotNull byte[] bytes) {
        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(bytes))
                    .ifPresent(packet -> {
                        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(serverPlayer.getInstance());
                        cancelPlayerCheckFuture(voicePlayer.getInstance().getUuid());

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

    public void onChannelsRegister(@NotNull McServerPlayer player, @NotNull List<String> channels) {
        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(player.getInstance());
        BaseVoice.DEBUG_LOGGER.log(
                "{} registered channels: {}. Response received: {}. Join packet sent: {}. Channel register packet sent: {}",
                player.getName(),
                player.getRegisteredChannels(),
                voicePlayer.getPublicKey().isPresent(),
                joinPacketSent.contains(player.getUuid()),
                channelRegisterPacketSent.contains(player.getUuid())
        );

        if (!channels.contains(BaseVoiceServer.FLAG_CHANNEL_STRING)) return;
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;
        // skip if requestPlayerInfo is not sent in onPlayerJoin
        if (!joinPacketSent.contains(player.getUuid())) return;
        if (channelRegisterPacketSent.contains(player.getUuid())) return;

        // skip if requestPlayerInfo already received from onPlayerJoin request
        if (voicePlayer.getPublicKey().isPresent()) return;

        channelRegisterPacketSent.add(player.getUuid());
        voiceServer.getTcpPacketManager().requestPlayerInfo(voicePlayer);
    }

    public void onPlayerJoin(@NotNull McPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(player.getInstance());
        joinPacketSent.add(player.getUuid());

        // just send info request when player joins the server,
        // because old method of checking for exact channels was causing some unpredictable behavior and bugs
        // this solution should be (hopefully) more consistent
        voiceServer.getBackgroundExecutor().execute(() ->
            voiceServer.getMinecraftServer().executeInMainThread(() -> {
                McServerPlayer serverPlayer = (McServerPlayer) player;
                if (serverPlayer.getRegisteredChannels().contains(BaseVoiceServer.FLAG_CHANNEL_STRING)) {
                    channelRegisterPacketSent.add(player.getUuid());
                }

                voiceServer.getTcpPacketManager().requestPlayerInfo(voicePlayer);
            })
        );

        if (shouldKick(player)) {
            cancelPlayerCheckFuture(player.getUuid());

            playerCheckFutures.put(player.getUuid(), voiceServer.getBackgroundExecutor().schedule(() -> {
                voiceServer.getMinecraftServer().executeInMainThread(() -> kickModRequired(player));
            }, voiceServer.getConfig().voice().clientModRequiredCheckTimeoutMs(), TimeUnit.MILLISECONDS));
        }
    }

    public void onPlayerQuit(@NotNull McPlayer player) {
        channels.remove(player.getUuid());
        joinPacketSent.remove(player.getUuid());
        channelRegisterPacketSent.remove(player.getUuid());
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
