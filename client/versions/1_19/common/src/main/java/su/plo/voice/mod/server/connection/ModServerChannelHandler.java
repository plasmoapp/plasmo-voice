package su.plo.voice.mod.server.connection;

import com.google.common.io.ByteStreams;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.server.entity.ModServerPlayer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.connection.BaseServerChannelHandler;
import su.plo.voice.server.connection.PlayerChannelHandler;

import java.io.IOException;
import java.util.List;

public abstract class ModServerChannelHandler extends BaseServerChannelHandler {

    protected ModServerChannelHandler(@NotNull BaseVoiceServer voiceServer) {
        super(voiceServer);
    }

    @Override
    protected void handleRegisterChannels(List<String> channels, VoicePlayer player) {
        super.handleRegisterChannels(channels, player);
        channels.forEach((channel) ->
                ((ModServerPlayer) player.getInstance()).addChannel(channel)
        );
    }

    protected void receive(ServerPlayer player, FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                    .ifPresent(packet -> {
                        LogManager.getLogger().info("Channel packet received {}", packet);

                        VoicePlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                player.getUUID(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        channel.handlePacket(packet);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
