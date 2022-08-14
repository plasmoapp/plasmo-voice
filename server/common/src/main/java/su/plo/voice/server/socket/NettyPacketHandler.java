package su.plo.voice.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoRequestPacket;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.socket.NettyPacketUdp;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class NettyPacketHandler extends SimpleChannelInboundHandler<NettyPacketUdp> {

    private final PlasmoVoiceServer voiceServer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyPacketUdp nettyPacket) throws Exception {
        PacketUdp packet = nettyPacket.getPacketUdp();

        UUID secret = packet.getSecret();

        Optional<UdpConnection> optConnection = voiceServer.getUdpConnectionManager().getConnectionBySecret(secret);
        if (optConnection.isPresent()) {
            UdpConnection connection = optConnection.get();

            if (!connection.getRemoteAddress().equals(nettyPacket.getSender())) {
                connection.setRemoteAddress(nettyPacket.getSender());
            }

            connection.handlePacket(packet.getPacket());
            return;
        }

        Optional<UUID> playerId = voiceServer.getUdpConnectionManager().getPlayerIdBySecret(secret);
        if (!playerId.isPresent()) return;

        Optional<VoicePlayer> player = voiceServer.getPlayerManager().getPlayerById(playerId.get());
        if (!player.isPresent()) return;

        NettyUdpConnection connection = new NettyUdpConnection(
                voiceServer,
                (NioDatagramChannel) ctx.channel(),
                secret,
                player.get()
        );
        connection.setRemoteAddress(nettyPacket.getSender());
        voiceServer.getUdpConnectionManager().addConnection(connection);

        player.get().sendPacket(new PlayerInfoRequestPacket());
    }
}
