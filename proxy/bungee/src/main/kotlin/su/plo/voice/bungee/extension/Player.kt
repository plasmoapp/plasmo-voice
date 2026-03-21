package su.plo.voice.bungee.extension

import net.md_5.bungee.api.connection.ProxiedPlayer
import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.proxy.player.VoiceProxyPlayer

/**
 * Gets a [VoiceProxyPlayer] of Bungee [ProxiedPlayer].
 *
 * @return The voice player.
 */
fun ProxiedPlayer.asVoicePlayer(voiceServer: PlasmoVoiceProxy): VoiceProxyPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)
