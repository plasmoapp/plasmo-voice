package su.plo.voice.api.server.audio.line

import su.plo.voice.api.server.player.VoicePlayer

interface ServerSourceLinePlayersSets {

    /**
     * Set your own implementation of [ServerPlayersSet] for this player
     *
     * @param player the player
     * @param playersSet the players set
     */
    fun setPlayersSet(player: VoicePlayer, playersSet: ServerPlayersSet?)

    fun getPlayersSet(player: VoicePlayer): ServerPlayersSet

    /**
     * Creates a map that will automatically broadcast about the changes to all players in the map
     *
     * @return the player map
     */
    fun createBroadcastSet(): ServerPlayersSet
}
