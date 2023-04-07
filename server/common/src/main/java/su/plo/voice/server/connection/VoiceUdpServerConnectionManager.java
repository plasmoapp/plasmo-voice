package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent;
import su.plo.voice.api.server.event.connection.UdpClientConnectedEvent;
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.ClientPacketUdpHandler;
import su.plo.voice.server.BaseVoiceServer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public final class VoiceUdpServerConnectionManager implements UdpServerConnectionManager {

    private final BaseVoiceServer voiceServer;

    private final Map<UUID, UUID> secretByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, UUID> playerIdBySecret = Maps.newConcurrentMap();

    private final Map<UUID, UdpServerConnection> connectionBySecret = Maps.newConcurrentMap();
    private final Map<UUID, UdpServerConnection> connectionByPlayerId = Maps.newConcurrentMap();

    public VoiceUdpServerConnectionManager(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        PlayerQuitEvent.INSTANCE.registerListener(player ->
                getConnectionByPlayerId(player.getUUID()).ifPresent(this::removeConnection)
        );
    }

    @Override
    public Optional<UUID> getPlayerIdBySecret(@NonNull UUID secret) {
        return Optional.ofNullable(playerIdBySecret.get(secret));
    }

    @Override
    public UUID getSecretByPlayerId(@NonNull UUID playerUUID) {
        if (secretByPlayerId.containsKey(playerUUID)) {
            return secretByPlayerId.get(playerUUID);
        }

        UUID secret = UUID.randomUUID();
        secretByPlayerId.put(playerUUID, secret);
        playerIdBySecret.put(secret, playerUUID);

        return secret;
    }

    @Override
    public void addConnection(@NonNull UdpServerConnection connection) {
        UdpClientConnectEvent connectEvent = new UdpClientConnectEvent(connection);
        if (!voiceServer.getEventBus().call(connectEvent)) return;

        UdpServerConnection bySecret = connectionBySecret.put(connection.getSecret(), connection);
        UdpServerConnection byPlayer = connectionByPlayerId.put(connection.getPlayer().getInstance().getUUID(), connection);

        if (bySecret != null) bySecret.disconnect();
        if (byPlayer != null) byPlayer.disconnect();

        BaseVoice.DEBUG_LOGGER.log("{} ({}) connected", connection.getPlayer().getInstance().getName(), connection.getRemoteAddress());
        voiceServer.getEventBus().call(new UdpClientConnectedEvent(connection));
    }

    @Override
    public boolean removeConnection(@NonNull UdpServerConnection connection) {
        UdpServerConnection bySecret = connectionBySecret.remove(connection.getSecret());
        UdpServerConnection byPlayer = connectionByPlayerId.remove(connection.getPlayer().getInstance().getUUID());

        if (bySecret != null) disconnect(bySecret);
        if (byPlayer != null && !byPlayer.equals(bySecret)) disconnect(byPlayer);

        return bySecret != null || byPlayer != null;
    }

    @Override
    public boolean removeConnection(VoiceServerPlayer player) {
        UdpServerConnection connection = connectionByPlayerId.remove(player.getInstance().getUUID());
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    @Override
    public boolean removeConnection(UUID secret) {
        UdpServerConnection connection = connectionBySecret.remove(secret);
        if (connection != null) disconnect(connection);

        return connection != null;
    }

    @Override
    public Optional<UdpServerConnection> getConnectionBySecret(@NotNull UUID secret) {
        return Optional.ofNullable(connectionBySecret.get(secret));
    }

    @Override
    public Optional<UdpServerConnection> getConnectionByPlayerId(@NotNull UUID playerId) {
        return Optional.ofNullable(connectionByPlayerId.get(playerId));
    }

    @Override
    public Collection<UdpServerConnection> getConnections() {
        return connectionByPlayerId.values();
    }

    @Override
    public void clearConnections() {
        getConnections().forEach(this::removeConnection);
    }

    private void disconnect(UdpServerConnection connection) {
        if (!connection.isConnected()) return;
        connection.disconnect();

        VoicePlayer player = connection.getPlayer();

        secretByPlayerId.remove(player.getInstance().getUUID());
        playerIdBySecret.remove(connection.getSecret());

        connectionByPlayerId.remove(player.getInstance().getUUID());
        connectionBySecret.remove(connection.getSecret());

        BaseVoice.DEBUG_LOGGER.log("{} disconnected", connection.getPlayer().getInstance().getName());
        voiceServer.getEventBus().call(new UdpClientDisconnectedEvent(connection));
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketUdpHandler> packet, @Nullable Predicate<VoiceServerPlayer> filter) {
        for (UdpServerConnection connection : getConnections()) {
            if (filter == null || filter.test(connection.getPlayer()))
                connection.sendPacket(packet);
        }
    }
}
