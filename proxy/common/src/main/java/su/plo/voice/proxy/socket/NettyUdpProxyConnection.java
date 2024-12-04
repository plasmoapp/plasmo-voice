package su.plo.voice.proxy.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.PacketUdpCodec;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;
import su.plo.voice.proxy.connection.CancelForwardingException;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class NettyUdpProxyConnection implements UdpProxyConnection, ServerPacketUdpHandler {

    private final PlasmoVoiceProxy voiceProxy;
    private final NioDatagramChannel channel;

    @Getter
    private final VoiceProxyPlayer player;
    @Getter
    private final UUID secret;

    @Getter @Setter
    private UUID remoteSecret;
    @Getter @Setter
    private InetSocketAddress remoteAddress;
    @Setter
    private RemoteServer remoteServer;
    private boolean connected = true;

    @Override
    public Optional<RemoteServer> getRemoteServer() {
        return Optional.ofNullable(remoteServer);
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (!isConnected()) return;

        byte[] encoded = PacketUdpCodec.encode(packet, secret);
        if (encoded == null) return;

        ByteBuf buf = Unpooled.wrappedBuffer(encoded);

        channel.writeAndFlush(new DatagramPacket(buf, remoteAddress));
    }

    @Override
    public void handlePacket(Packet<ServerPacketUdpHandler> packet) {
        packet.handle(this);
    }

    @Override
    public void disconnect() {
        this.connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected && player.getInstance().getServer() != null;
    }

    @Override
    public void handle(@NotNull PingPacket packet) {
    }

    @Override
    public void handle(@NotNull CustomPacket packet) {
    }

    @Override
    public void handle(@NotNull PlayerAudioPacket packet) {
        if (!voiceProxy.getEventBus().fire(new PlayerSpeakEvent(player, packet))) {
            throw new CancelForwardingException();
        }
    }
}
