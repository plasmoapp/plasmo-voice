package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.event.player.McPlayerQuitEvent;
import su.plo.slib.api.server.channel.McServerChannelHandler;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.slib.api.server.event.player.McPlayerRegisterChannelsEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.PacketDirection;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ServerChannelHandler implements McServerChannelHandler {

    private final BaseVoiceServer voiceServer;

    private final Map<UUID, PlayerChannelHandler> channels = Maps.newConcurrentMap();

    public ServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        McPlayerQuitEvent.INSTANCE.registerListener(this::onPlayerQuit);
        McPlayerRegisterChannelsEvent.INSTANCE.registerListener(this::onChannelsRegister);
    }

    @Override
    public void receive(@NotNull McServerPlayer serverPlayer, @NotNull byte[] bytes) {
        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(bytes), PacketDirection.SERVER)
                    .ifPresent(packet -> {
                        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(serverPlayer.getInstance());

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                serverPlayer.getUuid(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        channel.handlePacket(packet);
                    });
        } catch (Throwable e) {
            BaseVoice.DEBUG_LOGGER.warn("Failed to decode packet", e);
        }
    }

    public void clear() {
        channels.clear();
    }

    public void onChannelsRegister(@NotNull McServerPlayer player, @NotNull List<String> channels) {
        VoiceServerPlayer voicePlayer = voiceServer.getPlayerManager().getPlayerByInstance(player.getInstance());
        BaseVoice.DEBUG_LOGGER.log(
                "{} registered channels: {}. Response received: {}",
                player.getName(),
                player.getRegisteredChannels(),
                voicePlayer.getPublicKey().isPresent()
        );
    }

    public void onPlayerQuit(@NotNull McPlayer player) {
        channels.remove(player.getUuid());
    }
}
