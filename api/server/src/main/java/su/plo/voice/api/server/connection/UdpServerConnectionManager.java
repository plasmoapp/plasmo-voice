package su.plo.voice.api.server.connection;

import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

// todo: doc
public interface UdpServerConnectionManager extends ServerConnectionManager<ClientPacketUdpHandler> {

    Optional<UUID> getPlayerIdBySecret(UUID secret);

    UUID getSecretByPlayerId(UUID playerUUID);

    void addConnection(UdpConnection connection);

    boolean removeConnection(UdpConnection connection);

    boolean removeConnection(VoicePlayer player);

    boolean removeConnection(UUID secret);

    Optional<UdpConnection> getConnectionBySecret(UUID secret);

    Optional<UdpConnection> getConnectionByUUID(UUID playerUUID);

    Collection<UdpConnection> getConnections();

    void clearConnections();
}
