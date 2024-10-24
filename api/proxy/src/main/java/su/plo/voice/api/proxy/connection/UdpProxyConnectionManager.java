package su.plo.voice.api.proxy.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;
import su.plo.voice.api.server.connection.UdpConnectionManager;

import java.util.Optional;
import java.util.UUID;

/**
 * Manages UDP proxy connections.
 */
public interface UdpProxyConnectionManager extends UdpConnectionManager<VoiceProxyPlayer, UdpProxyConnection> {

    /**
     * Gets the player's unique identifier by their remote secret.
     *
     * <p>
     *     Remote secret is a secret, received from the remote backend and used for communication between proxy and backend.
     * </p>
     *
     * @param remoteSecret The remote secret of the player.
     * @return An optional containing the player's unique identifier if found, otherwise empty.
     */
    Optional<UUID> getPlayerIdByRemoteSecret(@NotNull UUID remoteSecret);

    /**
     * Gets the player's unique identifier by their proxy secret.
     *
     * <p>
     *     Proxy secret is a secret, generated by the proxy and used for communication between proxy and player.
     * </p>
     *
     * @param secret The proxy secret of the player.
     * @return An optional containing the player's unique identifier if found, otherwise empty.
     */
    Optional<UUID> getPlayerIdByProxySecret(@NotNull UUID secret);

    /**
     * Gets the player's unique identifier by any of their secrets.
     *
     * @param secret Any of the player's secrets.
     * @return An optional containing the player's unique identifier if found, otherwise empty.
     */
    Optional<UUID> getPlayerIdByAnySecret(@NotNull UUID secret);

    /**
     * Gets the proxy secret associated with a player's unique identifier.
     *
     * @param playerId The UUID of the player.
     * @return An optional containing the player's proxy secret if found, otherwise empty.
     */
    Optional<UUID> getProxySecretByPlayerId(@NotNull UUID playerId);

    /**
     * Gets the remote secret associated with a player's unique identifier.
     *
     * @param playerId The UUID of the player.
     * @return An optional containing the player's remote secret if found, otherwise empty.
     */
    Optional<UUID> getRemoteSecretByPlayerId(@NotNull UUID playerId);

    /**
     * Sets the player's remote secret for the given player UUID.
     *
     * @param playerUUID   The UUID of the player.
     * @param remoteSecret The remote secret to associate with the player.
     * @return Generated proxy secret.
     */
    @NotNull UUID setPlayerRemoteSecret(@NotNull UUID playerUUID, @NotNull UUID remoteSecret);

    /**
     * Adds a UDP proxy connection.
     *
     * @param connection The UDP proxy connection to add.
     */
    void addConnection(@NotNull UdpProxyConnection connection);

    /**
     * Removes a UDP proxy connection.
     *
     * @param connection The UDP proxy connection to remove.
     * @return {@code true} if the removal was successful, {@code false} if the connection was not found.
     */
    boolean removeConnection(@NotNull UdpProxyConnection connection);

    /**
     * Removes a UDP proxy connection associated with a player.
     *
     * @param player The voice proxy player whose connection should be removed.
     * @return {@code true} if the removal was successful, {@code false} if the connection was not found.
     */
    boolean removeConnection(@NotNull VoiceProxyPlayer player);

    /**
     * Retrieves a UDP proxy connection by the remote secret of the player.
     *
     * @param remoteSecret The remote secret of the player.
     * @return An optional containing the UDP proxy connection if found, otherwise empty.
     */
    Optional<UdpProxyConnection> getConnectionByRemoteSecret(@NotNull UUID remoteSecret);

    /**
     * Retrieves a UDP proxy connection by any of the player's secrets.
     *
     * @param anySecret Any of the player's secrets.
     * @return An optional containing the UDP proxy connection if found, otherwise empty.
     */
    Optional<UdpProxyConnection> getConnectionByAnySecret(@NotNull UUID anySecret);

    /**
     * Clears all UDP proxy connections.
     */
    void clearConnections();
}
