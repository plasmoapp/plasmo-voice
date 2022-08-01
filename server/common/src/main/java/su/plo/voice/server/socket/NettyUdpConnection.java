package su.plo.voice.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventBus;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketSendEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdp;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.PacketUdpHandler;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.util.UUID;

@AllArgsConstructor
public class NettyUdpConnection extends SimpleChannelInboundHandler<PacketUdp> implements UdpConnection, ServerPacketUdpHandler {

    private final EventBus eventBus;
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
    public void sendPacket(Packet<PacketUdpHandler> packet) {
        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        System.out.println("send to:" + channel.remoteAddress());

        channel.writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));

        UdpPacketSendEvent event = new UdpPacketSendEvent(this, packet);
        eventBus.call(event);
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PacketUdp packetUdp) throws Exception {
        Packet<ServerPacketUdpHandler> packet = packetUdp.getPacket();

        UdpPacketReceivedEvent event = new UdpPacketReceivedEvent(this, packet);
        eventBus.call(event);
        if (event.isCancelled()) return;

        packet.handle(this);
    }

    @Override
    public void handle(@NotNull PingPacket packet) {
    }

    @Override
    public void handle(@NotNull CustomPacket packet) {
    }
}
