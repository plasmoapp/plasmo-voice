package su.plo.voice.api.server.mute.storage;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.mute.ServerMuteInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores voice mutes.
 */
public interface MuteStorage {

    /**
     * Invoked when the server is starting.
     */
    default void init() throws Exception {
    }

    /**
     * Invoked when the server is stopping.
     */
    default void close() throws Exception {
    }

    /**
     * Puts the player's mute information into the storage.
     *
     * @param playerId The UUID of the player.
     * @param muteInfo The player's mute information.
     */
    void putPlayerMute(@NotNull UUID playerId, @NotNull ServerMuteInfo muteInfo);

    /**
     * Gets the player's mute information from storage by their UUID.
     *
     * @param playerId The UUID of the player.
     * @return An {@link Optional} containing {@link ServerMuteInfo} if the player is muted, or empty otherwise.
     */
    Optional<ServerMuteInfo> getMuteByPlayerId(@NotNull UUID playerId);

    /**
     * Removes the player's mute information from storage by their UUID.
     *
     * @param playerId The UUID of the player.
     * @return An {@link Optional} containing {@link ServerMuteInfo} if the player was muted and the mute information was removed, or empty otherwise.
     */
    Optional<ServerMuteInfo> removeMuteByPlayerId(@NotNull UUID playerId);

    /**
     * Gets a collection of all muted players.
     *
     * @return A collection of {@link ServerMuteInfo} representing the muted players.
     */
    Collection<ServerMuteInfo> getMutedPlayers();
}
