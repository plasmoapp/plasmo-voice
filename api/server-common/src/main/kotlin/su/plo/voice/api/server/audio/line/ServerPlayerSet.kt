package su.plo.voice.api.server.audio.line

import su.plo.voice.api.server.player.VoicePlayer
import java.util.*

/**
 * Represents a set of voice players.
 */
interface ServerPlayerSet {

    /**
     * Gets a collection of all the voice players in the set.
     *
     * @return A collection of voice players.
     */
    val players: Collection<VoicePlayer>

    /**
     * Adds a voice player to the set.
     *
     * @param player The voice player to add.
     */
    fun addPlayer(player: VoicePlayer)

    /**
     * Removes a voice player from the set by their UUID.
     *
     * @param playerId the UUID of the player to remove
     * @return `true` if the player was removed, `false` if the player was not found in the set.
     */
    fun removePlayer(playerId: UUID): Boolean

    /**
     * Clears all voice players from the set.
     */
    fun clearPlayers()
}
