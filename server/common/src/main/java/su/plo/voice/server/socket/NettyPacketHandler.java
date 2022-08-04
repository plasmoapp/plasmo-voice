package su.plo.voice.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.socket.NettyPacketUdp;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class NettyPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final EventBus eventBus;
    private final TcpServerConnectionManager tcpConnections;
    private final UdpServerConnectionManager udpConnections;
    private final PlayerManager players;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) throws Exception {
        PacketUdp packet = nettyPacket.getPacketUdp();

        UUID secret = packet.getSecret();

        Optional<UdpConnection> optConnection = udpConnections.getConnectionBySecret(secret);
        if (optConnection.isPresent()) {
            UdpConnection connection = optConnection.get();

            if (!connection.getRemoteAddress().equals(nettyPacket.getSender())) {
                connection.setRemoteAddress(nettyPacket.getSender());
            }

            connection.handlePacket(packet.getPacket());
            return;
        }

        Optional<UUID> playerId = udpConnections.getPlayerIdBySecret(secret);
        if (!playerId.isPresent()) return;

        Optional<VoicePlayer> player = players.getPlayer(playerId.get());
        if (!player.isPresent()) return;

        NettyUdpConnection connection = new NettyUdpConnection(
                eventBus,
                (NioDatagramChannel) ctx.channel(),
                secret,
                player.get()
        );
        connection.setRemoteAddress(nettyPacket.getSender());
        udpConnections.addConnection(connection);

        tcpConnections.sendConfigInfo(player.get());
    }
}
