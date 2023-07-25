package su.plo.voice.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.socket.NettyPacketUdp;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class NettyPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final BaseVoiceServer voiceServer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) throws Exception {
        PacketUdp packet = nettyPacket.getPacketUdp();

        UUID secret = packet.getSecret();

        if (voiceServer.getUdpConnectionManager().getConnectionBySecret(secret)
                .map(connection -> {
                    if (!connection.getRemoteAddress().equals(nettyPacket.getDatagramPacket().sender())) {
                        connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
                    }

                    try {
                        connection.handlePacket(packet.getPacket());
                    } catch (IOException e) {
                        BaseVoice.DEBUG_LOGGER.log("Failed to decode packet", e);
                    }

                    return true;
                })
                .orElse(false)
        ) return;

        Optional<UUID> playerId = voiceServer.getUdpConnectionManager().getPlayerIdBySecret(secret);
        if (!playerId.isPresent()) return;

        Optional<VoiceServerPlayer> player = voiceServer.getPlayerManager().getPlayerById(playerId.get());
        if (!player.isPresent()) return;

        NettyUdpServerConnection connection = new NettyUdpServerConnection(
                voiceServer,
                (NioDatagramChannel) ctx.channel(),
                secret,
                player.get()
        );
        connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
        voiceServer.getUdpConnectionManager().addConnection(connection);

        voiceServer.getTcpConnectionManager().sendConfigInfo(player.get());
        voiceServer.getTcpConnectionManager().sendPlayerList(player.get());

        voiceServer.getTcpConnectionManager().broadcastPlayerInfoUpdate(player.get());
    }
}
