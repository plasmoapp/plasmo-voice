package su.plo.voice.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.udp.PacketUdp;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class NettyHandshakeHandler extends SimpleChannelInboundHandler<PacketUdp> {

    private final ConnectionManager connections;
    private final PlayerManager players;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketUdp packet) throws Exception {
        UUID secret = packet.getSecret();

        Optional<UUID> playerId = connections.getPlayerIdBySecret(secret);
        if (!playerId.isPresent()) return;

        Optional<VoicePlayer> player = players.getPlayer(playerId);
        if (!player.isPresent()) return;

        NettyUdpConnection connection = new NettyUdpConnection((NioDatagramChannel) ctx.channel(), secret, player.get());
        connections.addConnection(connection);

        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.replace(this, "handler", connection);
    }
}
