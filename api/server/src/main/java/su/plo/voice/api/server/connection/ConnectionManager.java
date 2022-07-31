package su.plo.voice.api.server.connection;

import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;

import java.util.Optional;
import java.util.UUID;

// todo: doc
public interface ConnectionManager {

    Optional<UUID> getPlayerIdBySecret(UUID playerUUID);

    UUID getSecretByPlayerId(UUID secret);

    void addConnection(UdpConnection connection);

    boolean removeConnection(UdpConnection connection);

    boolean removeConnection(VoicePlayer player);

    boolean removeConnection(UUID secret);

    Optional<UdpConnection> getConnectionBySecret(UUID secret);

    Optional<UdpConnection> getConnectionByUUID(UUID playerUUID);
}
