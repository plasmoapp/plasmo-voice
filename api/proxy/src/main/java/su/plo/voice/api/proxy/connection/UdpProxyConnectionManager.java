package su.plo.voice.api.proxy.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.connection.UdpConnectionManager;

import java.util.Optional;
import java.util.UUID;

// todo: doc
public interface UdpProxyConnectionManager extends UdpConnectionManager<VoiceProxyPlayer, UdpProxyConnection> {

    Optional<UUID> getPlayerIdByRemoteSecret(UUID remoteSecret);

    Optional<UUID> getPlayerIdBySecret(UUID secret);

    Optional<UUID> getPlayerIdByAnySecret(UUID secret);

    Optional<UUID> getSecretByPlayerId(UUID playerId);

    Optional<UUID> getRemoteSecretByPlayerId(UUID playerId);

    @NotNull UUID setPlayerSecret(UUID playerUUID, UUID remoteSecret);

    void addConnection(UdpProxyConnection connection);

    boolean removeConnection(UdpProxyConnection connection);

    boolean removeConnection(VoiceProxyPlayer player);

    Optional<UdpProxyConnection> getConnectionByRemoteSecret(UUID remoteSecret);

    Optional<UdpProxyConnection> getConnectionByAnySecret(UUID anySecret);

    void clearConnections();
}
