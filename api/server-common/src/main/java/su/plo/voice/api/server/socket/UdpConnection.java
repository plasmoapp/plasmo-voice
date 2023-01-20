package su.plo.voice.api.server.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

// todo: doc
public interface UdpConnection<P extends VoicePlayer<?>> {

    @NotNull UUID getSecret();

    @NotNull P getPlayer();

    @NotNull InetSocketAddress getRemoteAddress();

    void setRemoteAddress(@NotNull InetSocketAddress remoteAddress);

    void sendPacket(Packet<?> packet);

    void handlePacket(Packet<ServerPacketUdpHandler> packet);

    void disconnect();

    boolean isConnected();
}
