package su.plo.voice.minestom.extension

import net.minestom.server.entity.Player
import su.plo.voice.api.server.player.VoiceServerPlayer

/**
 * Gets a Minestom [Player] of [VoiceServerPlayer].
 *
 * @return The Minestom player.
 */
fun VoiceServerPlayer.asMinestomPlayer(): Player =
    instance.getInstance()
