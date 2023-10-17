package su.plo.voice.api.server.audio.line

import su.plo.voice.api.server.player.VoicePlayer

/**
 * Manages the sets of players associated with a source line.
 *
 * Default implementations automatically send update packets to associated players.
 * Players from the sets are visible in the client-side overlay.
 */
interface ServerPlayerSetManager {

    /**
     * Sets your own implementation of [ServerPlayerSet] for a specific player.
     *
     * @param player the player for whom to set the player set.
     * @param playerSet the player set to associate with the player.
     */
    fun setPlayerSet(player: VoicePlayer, playerSet: ServerPlayerSet?)

    /**
     * Gets the player set associated with a player.
     *
     * By default, [getPlayerSet] creates a unique set of players for each player, but you can change this behavior
     * by setting your own implementation of [ServerPlayerSet] for the player using [setPlayerSet].
     *
     * @param player the player for whom to get the player set.
     * @return the player set associated with the player.
     */
    fun getPlayerSet(player: VoicePlayer): ServerPlayerSet

    /**
     * Creates a map that will automatically broadcast changes to all players in the set.
     *
     * @return the player set with automatic broadcast functionality
     */
    fun createBroadcastSet(): ServerPlayerSet
}
