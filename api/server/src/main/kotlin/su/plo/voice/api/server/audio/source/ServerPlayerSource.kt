package su.plo.voice.api.server.audio.source

import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo

/**
 * Represents a player audio source.
 */
interface ServerPlayerSource : ServerPositionalSource<PlayerSourceInfo> {

    /**
     * Gets the player associated with this audio source.
     *
     * @return The player.
     */
    val player: VoiceServerPlayer
}
