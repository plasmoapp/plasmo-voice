package su.plo.voice.proxy.connection;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.event.player.McPlayerQuitEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent;
import su.plo.voice.api.server.event.connection.UdpClientConnectedEvent;
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;
import su.plo.voice.proxy.BaseVoiceProxy;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class VoiceUdpProxyConnectionManager implements UdpProxyConnectionManager {

    private final BaseVoiceProxy voiceProxy;

    private final Map<UUID, UUID> remoteSecretByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, UUID> proxySecretByPlayerId = Maps.newConcurrentMap();

    private final Map<UUID, UUID> playerIdByRemoteSecret = Maps.newConcurrentMap();
    private final Map<UUID, UUID> playerIdByProxySecret = Maps.newConcurrentMap();

    private final Map<UUID, UdpProxyConnection> connectionByRemoteSecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpProxyConnection> connectionByProxySecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpProxyConnection> connectionByPlayerId = Maps.newConcurrentMap();

    public VoiceUdpProxyConnectionManager(@NotNull BaseVoiceProxy voiceProxy) {
        this.voiceProxy = voiceProxy;

        McPlayerQuitEvent.INSTANCE.registerListener(this::onPlayerQuit);
    }

    @Override
    public Optional<UUID> getPlayerIdByRemoteSecret(@NotNull UUID remoteSecret) {
        return Optional.ofNullable(playerIdByRemoteSecret.get(remoteSecret));
    }

    @Override
    public Optional<UUID> getPlayerIdByProxySecret(@NotNull UUID secret) {
        return Optional.ofNullable(playerIdByProxySecret.get(secret));
    }

    @Override
    public Optional<UUID> getPlayerIdByAnySecret(@NotNull UUID secret) {
        UUID playerId = playerIdByProxySecret.get(secret);
        if (playerId != null) return Optional.of(playerId);

        return Optional.ofNullable(playerIdByRemoteSecret.get(secret));
    }

    @Override
    public Optional<UUID> getProxySecretByPlayerId(@NotNull UUID playerId) {
        return Optional.ofNullable(proxySecretByPlayerId.get(playerId));
    }

    @Override
    public Optional<UUID> getRemoteSecretByPlayerId(@NotNull UUID playerId) {
        return Optional.ofNullable(remoteSecretByPlayerId.get(playerId));
    }

    @Override
    public @NotNull UUID setPlayerRemoteSecret(@NotNull UUID playerUUID, @NotNull UUID remoteSecret) {
        UUID oldSecret = remoteSecretByPlayerId.put(playerUUID, remoteSecret);
        playerIdByRemoteSecret.put(remoteSecret, playerUUID);

        if (oldSecret == null) {
            UUID secret = UUID.randomUUID();

            playerIdByProxySecret.put(secret, playerUUID);
            proxySecretByPlayerId.put(playerUUID, secret);
            return secret;
        }

        getConnectionByRemoteSecret(oldSecret).ifPresent(connection -> {
            connection.setRemoteSecret(remoteSecret);
            connectionByRemoteSecret.remove(oldSecret);
            connectionByRemoteSecret.put(remoteSecret, connection);
        });

        return proxySecretByPlayerId.get(playerUUID);
    }

    @Override
    public void addConnection(@NotNull UdpProxyConnection connection) {
        UdpClientConnectEvent connectEvent = new UdpClientConnectEvent(connection);
        if (!voiceProxy.getEventBus().fire(connectEvent)) return;

        UdpProxyConnection bySecret = connectionByProxySecret.put(connection.getSecret(), connection);
        UdpProxyConnection byRemoteSecret = connectionByRemoteSecret.put(connection.getRemoteSecret(), connection);
        UdpProxyConnection byPlayer = connectionByPlayerId.put(connection.getPlayer().getInstance().getUuid(), connection);

        if (bySecret != null) bySecret.disconnect();
        if (byRemoteSecret != null) byRemoteSecret.disconnect();
        if (byPlayer != null) byPlayer.disconnect();

        BaseVoice.DEBUG_LOGGER.log(
                "{} ({}) connected",
                connection.getPlayer().getInstance().getName(),
                connection.getRemoteAddress()
        );
        voiceProxy.getEventBus().fire(new UdpClientConnectedEvent(connection));
    }

    @Override
    public boolean removeConnection(@NotNull UdpProxyConnection connection) {
        UdpProxyConnection bySecret = connectionByProxySecret.remove(connection.getSecret());
        UdpProxyConnection byRemoteSecret = connectionByRemoteSecret.remove(connection.getRemoteSecret());
        UdpProxyConnection byPlayer = connectionByPlayerId.remove(connection.getPlayer().getInstance().getUuid());

        if (bySecret != null) disconnect(bySecret);
        if (byRemoteSecret != null) disconnect(byRemoteSecret);
        if (byPlayer != null && !byPlayer.equals(bySecret)) disconnect(byPlayer);

        return bySecret != null || byPlayer != null;
    }

    @Override
    public boolean removeConnection(@NotNull VoiceProxyPlayer player) {
        UdpProxyConnection connection = connectionByPlayerId.remove(player.getInstance().getUuid());
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    public Optional<UdpProxyConnection> getConnectionByRemoteSecret(@NotNull UUID secret) {
        return Optional.ofNullable(connectionByRemoteSecret.get(secret));
    }

    @Override
    public Optional<UdpProxyConnection> getConnectionBySecret(@NotNull UUID secret) {
        return Optional.ofNullable(connectionByProxySecret.get(secret));
    }

    @Override
    public Optional<UdpProxyConnection> getConnectionByAnySecret(@NotNull UUID anySecret) {
        UdpProxyConnection connection = connectionByProxySecret.get(anySecret);
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

    private void disconnect(@NotNull UdpProxyConnection connection) {
        if (!connection.isConnected()) return;
        connection.disconnect();

        proxySecretByPlayerId.remove(connection.getPlayer().getInstance().getUuid());
        remoteSecretByPlayerId.remove(connection.getPlayer().getInstance().getUuid());
        playerIdByProxySecret.remove(connection.getRemoteSecret());
        playerIdByRemoteSecret.remove(connection.getSecret());

        BaseVoice.DEBUG_LOGGER.log("{} disconnected", connection.getPlayer().getInstance().getName());
        voiceProxy.getEventBus().fire(new UdpClientDisconnectedEvent(connection));
    }

    private void onPlayerQuit(@NotNull McPlayer player) {
        getConnectionByPlayerId(player.getUuid()).ifPresent(this::removeConnection);

        UUID remoteSecret = remoteSecretByPlayerId.remove(player.getUuid());
        UUID playerId = proxySecretByPlayerId.remove(player.getUuid());

        if (remoteSecret != null) playerIdByRemoteSecret.remove(remoteSecret);
        if (playerId != null) playerIdByProxySecret.remove(playerId);
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketUdpHandler> packet, @Nullable Predicate<VoiceProxyPlayer> filter) {
        for (UdpProxyConnection connection : getConnections()) {
            if (filter == null || filter.test(connection.getPlayer()))
                connection.sendPacket(packet);
        }
    }
}
