package su.plo.voice.api.server.connection;

import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServerConnection;

import java.util.Optional;
import java.util.UUID;

// todo: doc
public interface UdpServerConnectionManager extends UdpConnectionManager<VoiceServerPlayer, UdpServerConnection> {

    Optional<UUID> getPlayerIdBySecret(UUID secret);

    UUID getSecretByPlayerId(UUID playerUUID);

    void addConnection(UdpServerConnection connection);

    boolean removeConnection(UdpServerConnection connection);

    boolean removeConnection(VoiceServerPlayer player);

    boolean removeConnection(UUID secret);

    void clearConnections();
}
