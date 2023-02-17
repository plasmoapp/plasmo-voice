package su.plo.voice.proxy.connection;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.event.connection.UdpClientConnectEvent;
import su.plo.voice.api.proxy.event.connection.UdpClientConnectedEvent;
import su.plo.voice.api.proxy.event.connection.UdpClientDisconnectedEvent;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;
import su.plo.voice.proxy.BaseVoiceProxy;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@AllArgsConstructor
public final class VoiceUdpProxyConnectionManager implements UdpProxyConnectionManager {

    private final BaseVoiceProxy voiceProxy;

    private final Map<UUID, UUID> remoteSecretByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, UUID> secretByPlayerId = Maps.newConcurrentMap();

    private final Map<UUID, UUID> playerIdByRemoteSecret = Maps.newConcurrentMap();
    private final Map<UUID, UUID> playerIdBySecret = Maps.newConcurrentMap();

    private final Map<UUID, UdpProxyConnection> connectionByRemoteSecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpProxyConnection> connectionBySecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpProxyConnection> connectionByPlayerId = Maps.newConcurrentMap();

    @Override
    public Optional<UUID> getPlayerIdByRemoteSecret(UUID remoteSecret) {
        return Optional.ofNullable(playerIdByRemoteSecret.get(remoteSecret));
    }

    @Override
    public Optional<UUID> getPlayerIdBySecret(UUID secret) {
        return Optional.ofNullable(playerIdBySecret.get(secret));
    }

    @Override
    public Optional<UUID> getPlayerIdByAnySecret(UUID secret) {
        UUID playerId = playerIdBySecret.get(secret);
        if (playerId != null) return Optional.of(playerId);

        return Optional.ofNullable(playerIdByRemoteSecret.get(secret));
    }

    @Override
    public Optional<UUID> getSecretByPlayerId(UUID playerId) {
        return Optional.ofNullable(secretByPlayerId.get(playerId));
    }

    @Override
    public Optional<UUID> getRemoteSecretByPlayerId(UUID playerId) {
        return Optional.ofNullable(remoteSecretByPlayerId.get(playerId));
    }

    @Override
    public @NotNull UUID setPlayerSecret(UUID playerUUID, UUID remoteSecret) {
        UUID oldSecret = remoteSecretByPlayerId.put(playerUUID, remoteSecret);
        playerIdByRemoteSecret.put(remoteSecret, playerUUID);

        if (oldSecret == null) {
            UUID secret = UUID.randomUUID();

            playerIdBySecret.put(secret, playerUUID);
            secretByPlayerId.put(playerUUID, secret);
            return secret;
        }

        getConnectionByRemoteSecret(oldSecret).ifPresent(connection -> {
            connection.setRemoteSecret(remoteSecret);
            connectionByRemoteSecret.remove(oldSecret);
            connectionByRemoteSecret.put(remoteSecret, connection);
        });

        return secretByPlayerId.get(playerUUID);
    }

    @Override
    public void addConnection(UdpProxyConnection connection) {
        UdpClientConnectEvent connectEvent = new UdpClientConnectEvent(connection);
        if (!voiceProxy.getEventBus().call(connectEvent)) return;

        UdpProxyConnection bySecret = connectionBySecret.put(connection.getSecret(), connection);
        UdpProxyConnection byRemoteSecret = connectionByRemoteSecret.put(connection.getRemoteSecret(), connection);
        UdpProxyConnection byPlayer = connectionByPlayerId.put(connection.getPlayer().getInstance().getUUID(), connection);

        if (bySecret != null) bySecret.disconnect();
        if (byRemoteSecret != null) byRemoteSecret.disconnect();
        if (byPlayer != null) byPlayer.disconnect();

        voiceProxy.getLogger().debug("{} ({}) connected", connection.getPlayer(), connection.getRemoteAddress());
        voiceProxy.getEventBus().call(new UdpClientConnectedEvent(connection));
    }

    @Override
    public boolean removeConnection(UdpProxyConnection connection) {
        UdpProxyConnection bySecret = connectionBySecret.remove(connection.getSecret());
        UdpProxyConnection byRemoteSecret = connectionByRemoteSecret.remove(connection.getRemoteSecret());
        UdpProxyConnection byPlayer = connectionByPlayerId.remove(connection.getPlayer().getInstance().getUUID());

        if (bySecret != null) disconnect(bySecret);
        if (byRemoteSecret != null) disconnect(byRemoteSecret);
        if (byPlayer != null && !byPlayer.equals(bySecret)) disconnect(byPlayer);

        return bySecret != null || byPlayer != null;
    }

    @Override
    public boolean removeConnection(VoiceProxyPlayer player) {
        UdpProxyConnection connection = connectionByPlayerId.remove(player.getInstance().getUUID());
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    public Optional<UdpProxyConnection> getConnectionByRemoteSecret(UUID secret) {
        return Optional.ofNullable(connectionByRemoteSecret.get(secret));
    }

    @Override
    public Optional<UdpProxyConnection> getConnectionBySecret(@NotNull UUID secret) {
        return Optional.ofNullable(connectionBySecret.get(secret));
    }

    @Override
    public Optional<UdpProxyConnection> getConnectionByAnySecret(UUID anySecret) {
        UdpProxyConnection connection = connectionBySecret.get(anySecret);
        if (connection != null) return Optional.of(connection);

        return Optional.ofNullable(connectionByRemoteSecret.get(anySecret));
    }

    @Override
    public Optional<UdpProxyConnection> getConnectionByPlayerId(@NotNull UUID playerId) {
        return Optional.ofNullable(connectionByPlayerId.get(playerId));
    }

    @Override
    public Collection<UdpProxyConnection> getConnections() {
        return connectionByPlayerId.values();
    }

    @Override
    public void clearConnections() {
        getConnections().forEach(this::removeConnection);
    }

    private void disconnect(UdpProxyConnection connection) {
        connection.disconnect();

        secretByPlayerId.remove(connection.getPlayer().getInstance().getUUID());
        remoteSecretByPlayerId.remove(connection.getPlayer().getInstance().getUUID());
        playerIdBySecret.remove(connection.getRemoteSecret());
        playerIdByRemoteSecret.remove(connection.getSecret());

        voiceProxy.getLogger().info("{} disconnected", connection.getPlayer());
        voiceProxy.getEventBus().call(new UdpClientDisconnectedEvent(connection));
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketUdpHandler> packet, @Nullable Predicate<VoiceProxyPlayer> filter) {
        for (UdpProxyConnection connection : getConnections()) {
            if (filter == null || filter.test(connection.getPlayer()))
                connection.sendPacket(packet);
        }
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketUdpHandler> packet) {
        broadcast(packet, null);
    }

    @EventSubscribe
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        getConnectionByPlayerId(event.getPlayerId()).ifPresent(this::removeConnection);
    }
}
