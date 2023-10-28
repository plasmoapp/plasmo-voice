package su.plo.voice.api.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.entity.player.McGameProfile;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a set of players.
 */
public interface ClientPlayerSet {

    /**
     * Adds a player's game profile to the set.
     *
     * @param player The game profile of the player to add.
     */
    void addPlayer(@NotNull McGameProfile player);

    /**
     * Removes a player from the set by their unique player ID.
     *
     * @param playerId The UUID of the player to remove.
     * @return {@code true} if the player was successfully removed, {@code false} otherwise.
     */
    boolean removePlayer(@NotNull UUID playerId);

    /**
     * Clears all players from the set.
     */
    void clearPlayers();

    /**
     * Gets a collection of game profiles representing the players in the set.
     *
     * @return A collection of player game profiles.
     */
    @NotNull Collection<McGameProfile> getPlayers();
}
