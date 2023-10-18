package su.plo.voice.api.server.audio.source

import su.plo.voice.api.server.player.VoicePlayer
import java.util.function.Supplier

/**
 * Represents a broadcast audio source.
 *
 * This source broadcasts audio and source packets to the specific group of players.
 */
interface ServerBroadcastSource : BaseServerDirectSource {

    /**
     * Gets or sets the supplier of players.
     *
     * If null, all players with Plasmo Voice will be used.
     *
     * @return The supplier of players.
     */
    var players: Supplier<Iterable<VoicePlayer>>?
}
