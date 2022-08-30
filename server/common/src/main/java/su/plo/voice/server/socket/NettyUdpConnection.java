package su.plo.voice.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.source.ServerPlayerSource;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketSendEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

@RequiredArgsConstructor
@ToString(of = {"channel", "secret", "player", "keepAlive", "sentKeepAlive"})
public final class NettyUdpConnection implements UdpConnection, ServerPacketUdpHandler {

    private final PlasmoVoiceServer voiceServer;
    private final NioDatagramChannel channel;

    @Getter
    @Setter
    private InetSocketAddress remoteAddress;
    @Getter
    private final UUID secret;
    @Getter
    private final VoicePlayer player;
    @Getter
    private long keepAlive = System.currentTimeMillis();
    @Getter
    @Setter
    private long sentKeepAlive;

    @Getter
    private boolean connected = true;

    @Override
    public void sendPacket(Packet<?> packet) {
        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        LogManager.getLogger().debug("UDP packet {} sent to {}", packet, remoteAddress);

        channel.writeAndFlush(new DatagramPacket(buf, remoteAddress));

        UdpPacketSendEvent event = new UdpPacketSendEvent(this, packet);
        voiceServer.getEventBus().call(event);
    }

    @Override
    public void handlePacket(Packet<ServerPacketUdpHandler> packet) {
        UdpPacketReceivedEvent event = new UdpPacketReceivedEvent(this, packet);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        packet.handle(this);
    }

    @Override
    public void disconnect() {
        channel.disconnect();
        connected = false;
    }

    @Override
    public void handle(@NotNull PingPacket packet) {
        this.keepAlive = System.currentTimeMillis();
    }

    @Override
    public void handle(@NotNull CustomPacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerAudioPacket packet) {
        ServerPlayerSource source = voiceServer.getSourceManager().getOrCreatePlayerSource(player, "opus");
        SourceAudioPacket sourcePacket = new SourceAudioPacket(
                packet.getSequenceNumber(),
                (byte) source.getState(),
                packet.getData(),
                source.getId(),
                packet.getDistance()
        );
        source.sendAudioPacket(sourcePacket, packet.getDistance());
    }
}
