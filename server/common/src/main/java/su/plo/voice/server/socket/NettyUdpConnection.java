package su.plo.voice.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.cllientbound.ClientPacketHandler;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketHandler;
import su.plo.voice.proto.packets.udp.serverbound.ServerPingPacket;

import java.util.UUID;

@AllArgsConstructor
public class NettyUdpConnection extends SimpleChannelInboundHandler<PacketUdp> implements UdpConnection, ServerPacketHandler {

    private final NioDatagramChannel channel;
    private final UUID secret;
    private final VoicePlayer player;

    @Override
    public UUID getSecret() {
        return secret;
    }

    @Override
    public VoicePlayer getPlayer() {
        return player;
    }

    @Override
    public void sendPacket(Packet<ClientPacketHandler> packet) {
        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        System.out.println("send to:" + channel.remoteAddress());

        channel.writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketUdp packet) throws Exception {

    }

    @Override
    public void handle(@NotNull ServerPingPacket packet) {

    }
}
