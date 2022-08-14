package su.plo.voice.api.server.socket;

import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.serverbound.ServerPacketUdpHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

// todo: doc
public interface UdpConnection {
    UUID getSecret();

    VoicePlayer getPlayer();

    InetSocketAddress getRemoteAddress();

    void setRemoteAddress(InetSocketAddress remoteAddress);

    long getKeepAlive();

    long getSentKeepAlive();

    void setSentKeepAlive(long keepAlive);

    void sendPacket(Packet<?> packet);

    void handlePacket(Packet<ServerPacketUdpHandler> packet);

    void disconnect();

    boolean isConnected();
}
