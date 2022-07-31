package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.connection.ConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class VoiceConnectionManager implements ConnectionManager {

    private final PlasmoVoiceServer server;

    private final Map<UUID, UUID> secretByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, UUID> playerIdBySecret = Maps.newConcurrentMap();

    private final Map<UUID, UdpConnection> connectionBySecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpConnection> connectionByPlayerId = Maps.newConcurrentMap();

    @Override
    public Optional<UUID> getPlayerIdBySecret(UUID secret) {
        return Optional.ofNullable(playerIdBySecret.get(secret));
    }

    @Override
    public UUID getSecretByPlayerId(UUID playerUUID) {
        if (secretByPlayerId.containsKey(playerUUID)) {
            return secretByPlayerId.get(playerUUID);
        }

        UUID secret = UUID.randomUUID();
        secretByPlayerId.put(playerUUID, secret);
        playerIdBySecret.put(secret, playerUUID);

        return secret;
    }

    @Override
    public void addConnection(UdpConnection connection) {
        UdpConnection bySecret = connectionBySecret.put(connection.getSecret(), connection);
        UdpConnection byPlayer = connectionByPlayerId.put(connection.getPlayer().getUUID(), connection);

        if (bySecret != null) bySecret.disconnect();
        if (byPlayer != null) byPlayer.disconnect();
    }

    @Override
    public boolean removeConnection(UdpConnection connection) {
        boolean bySecret = removeConnection(connection.getSecret());
        boolean byPlayer = removeConnection(connection.getPlayer());

        return bySecret || byPlayer;
    }

    @Override
    public boolean removeConnection(VoicePlayer player) {
        UdpConnection connection = connectionByPlayerId.remove(player.getUUID());
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    @Override
    public boolean removeConnection(UUID secret) {
        UdpConnection connection = connectionBySecret.remove(secret);
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    @Override
    public Optional<UdpConnection> getConnectionBySecret(UUID secret) {
        return Optional.ofNullable(connectionBySecret.get(secret));
    }

    @Override
    public Optional<UdpConnection> getConnectionByUUID(UUID playerUUID) {
        return Optional.ofNullable(connectionByPlayerId.get(playerUUID));
    }

    private void disconnect(UdpConnection connection) {
        connection.disconnect();

        secretByPlayerId.remove(connection.getPlayer().getUUID());
        playerIdBySecret.remove(connection.getSecret());
    }
}
