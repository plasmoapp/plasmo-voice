package su.plo.voice.minestom.extension

import net.minestom.server.entity.Player
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.api.context.PlasmoVoiceHolder

/**
 * Gets a [VoiceServerPlayer] of Minestom [Player].
 *
 * @return The voice player.
 */
fun Player.asVoicePlayer(voiceServer: PlasmoVoiceServer): VoiceServerPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)

/**
 * Gets a [VoiceServerPlayer] of Minestom [Player].
 *
 * @return The voice player.
 */
context (PlasmoVoiceHolder<PlasmoVoiceServer>)
fun Player.asVoicePlayer(): VoiceServerPlayer =
    asVoicePlayer(this@PlasmoVoiceHolder.voiceInstance)

/**
 * Gets a [VoiceServerPlayer] of Minestom [Player].
 *
 * @return The voice player.
 */
context (PlasmoVoiceServer)
fun Player.asVoicePlayer(): VoiceServerPlayer =
    asVoicePlayer(this@PlasmoVoiceServer)
