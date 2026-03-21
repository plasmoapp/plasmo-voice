package su.plo.voice.paper.extension

import org.bukkit.entity.Player
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoiceServerPlayer

/**
 * Gets a [VoiceServerPlayer] of Bukkit [Player].
 *
 * @return The voice player.
 */
fun Player.asVoicePlayer(voiceServer: PlasmoVoiceServer): VoiceServerPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)
