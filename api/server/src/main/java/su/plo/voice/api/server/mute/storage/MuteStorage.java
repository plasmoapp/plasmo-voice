package su.plo.voice.api.server.mute.storage;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.mute.ServerMuteInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface MuteStorage {

    /**
     * Called when server is starting
     */
    default void init() throws Exception {
    }

    /**
     * Called when server is stopping
     */
    default void close() throws Exception {
    }

    /**
     * Puts the player mute to the storage
     *
     * @param playerId player uuid
     * @param muteInfo the player mute info
     */
    void putPlayerMute(@NotNull UUID playerId, @NotNull ServerMuteInfo muteInfo);

    /**
     * Gets the player mute from storage by his uuid
     *
     * @param playerId player uuid
     *
     * @return {@link ServerMuteInfo} if player is muted
     */
    Optional<ServerMuteInfo> getMuteByPlayerId(@NotNull UUID playerId);

    /**
     * Removes the player mute from storage by his uuid
     *
     * @param playerId player uuid
     *
     * @return {@link ServerMuteInfo} if player was muted
     */
    Optional<ServerMuteInfo> removeMuteByPlayerId(@NotNull UUID playerId);

    /**
     * Collection of the muted players
     */
    Collection<ServerMuteInfo> getMutedPlayers();
}
