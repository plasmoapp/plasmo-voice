package su.plo.voice.minestom.extension

import net.minestom.server.entity.Player
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoiceServerPlayer

/**
 * Gets a [VoiceServerPlayer] of Minestom [Player].
 *
 * @return The voice player.
 */
fun Player.asVoicePlayer(voiceServer: PlasmoVoiceServer): VoiceServerPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)
