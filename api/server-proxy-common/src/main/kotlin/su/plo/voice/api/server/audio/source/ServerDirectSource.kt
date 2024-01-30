package su.plo.voice.api.server.audio.source

import su.plo.voice.api.server.player.VoicePlayer

/**
 * Represents a direct audio source attached to the player.
 *
 * Audio and source packets will be sent to this player.
 */
interface ServerDirectSource : BaseServerDirectSource {

    /**
     * Gets a player attached to this source.
     *
     * @return The voice player.
     */
    val player: VoicePlayer
}
