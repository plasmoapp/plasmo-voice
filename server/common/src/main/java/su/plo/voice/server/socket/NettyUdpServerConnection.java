package su.plo.voice.server.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketSendEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.net.InetSocketAddress;
import java.util.UUID;

@ToString(of = {"channel", "secret", "player", "keepAlive", "sentKeepAlive"})
public final class NettyUdpServerConnection implements UdpServerConnection, ServerPacketUdpHandler {

    private final BaseVoiceServer voiceServer;
    private final NioDatagramChannel channel;

    @Getter
    private InetSocketAddress remoteAddress;
    @Getter
    private final UUID secret;
    @Getter
    private final VoiceServerPlayer player;
    @Getter
    private long keepAlive = System.currentTimeMillis();
    @Getter
    @Setter
    private long sentKeepAlive;

    @Getter
    private boolean connected = true;

    public NettyUdpServerConnection(@NotNull BaseVoiceServer voiceServer,
                                    @NotNull NioDatagramChannel channel,
                                    @NotNull UUID secret,
                                    @NotNull VoiceServerPlayer player) {
        this.voiceServer = voiceServer;
        this.channel = channel;
        this.secret = secret;
        this.player = player;
    }

    @Override
    public void setRemoteAddress(@NotNull InetSocketAddress remoteAddress) {
        voiceServer.getLogger().debug("Set remote address for {} from {} to {}",
                player,
                this.remoteAddress, remoteAddress
        );
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);
        channel.writeAndFlush(new DatagramPacket(buf, remoteAddress));

        UdpPacketSendEvent event = new UdpPacketSendEvent(this, packet);
        voiceServer.getEventBus().call(event);
    }

    @Override
    public void handlePacket(Packet<ServerPacketUdpHandler> packet) {
        UdpPacketReceivedEvent event = new UdpPacketReceivedEvent(this, packet);
        if (!voiceServer.getEventBus().call(event)) return;

        packet.handle(this);
    }

    @Override
    public void disconnect() {
        channel.disconnect();
        connected = false;

        voiceServer.getTcpConnectionManager().broadcast(new PlayerDisconnectPacket(player.getInstance().getUUID()));
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
        voiceServer.getEventBus().call(new PlayerSpeakEvent(player, packet));
    }
}
