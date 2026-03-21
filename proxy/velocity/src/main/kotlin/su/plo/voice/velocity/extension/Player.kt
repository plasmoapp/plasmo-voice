package su.plo.voice.velocity.extension

import com.velocitypowered.api.proxy.Player
import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.proxy.player.VoiceProxyPlayer

/**
 * Gets a [VoiceProxyPlayer] of Velocity [Player].
 *
 * @return The voice player.
 */
fun Player.asVoicePlayer(voiceServer: PlasmoVoiceProxy): VoiceProxyPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)
