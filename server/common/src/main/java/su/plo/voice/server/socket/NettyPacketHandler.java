package su.plo.voice.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.socket.NettyPacketUdp;

import java.io.IOException;
import java.net.InetSocketAddress;
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
                (DatagramChannel) ctx.channel(),
                secret,
                player.get()
        );
        connection.setRemoteAddress(nettyPacket.getDatagramPacket().sender());
        if (packet.getPacketUntyped() instanceof PingPacket) {
            PingPacket pingPacket = (PingPacket) packet.getPacketUntyped();
            if (pingPacket.getServerIp() != null) {
                connection.setConnectionAddress(new InetSocketAddress(pingPacket.getServerIp(), pingPacket.getServerPort()));
            }
        }
        voiceServer.getUdpConnectionManager().addConnection(connection);

        voiceServer.getTcpPacketManager().sendConfigInfo(player.get());
        voiceServer.getTcpPacketManager().sendPlayerList(player.get());

        voiceServer.getTcpPacketManager().broadcastPlayerInfoUpdate(player.get());
    }
}
