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
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;

import java.io.IOException;
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

    public void onPlayerJoin(@NotNull McPlayer player) {
        if (!voiceServer.getUdpServer().isPresent() || voiceServer.getConfig() == null) return;

        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(player.getInstance());

        // just send info request when player joins the server,
        // because old method of checking for exact channels was causing some unpredictable behavior and bugs
        // this solution should be (hopefully) more consistent
        voiceServer.getMinecraftServer().executeInMainThread(() ->
                voiceServer.getTcpPacketManager().requestPlayerInfo(voicePlayer)
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
