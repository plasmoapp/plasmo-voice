package su.plo.voice.bungee.extension

import net.md_5.bungee.api.connection.ProxiedPlayer
import su.plo.voice.api.context.PlasmoVoiceHolder
import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.proxy.player.VoiceProxyPlayer

/**
 * Gets a [VoiceProxyPlayer] of Bungee [ProxiedPlayer].
 *
 * @return The voice player.
 */
fun ProxiedPlayer.asVoicePlayer(voiceServer: PlasmoVoiceProxy): VoiceProxyPlayer =
    voiceServer.playerManager.getPlayerByInstance(this)

/**
 * Gets a [VoiceProxyPlayer] of Bungee [ProxiedPlayer].
 *
 * @return The voice player.
 */
context (PlasmoVoiceHolder<PlasmoVoiceProxy>)
fun ProxiedPlayer.asVoicePlayer(): VoiceProxyPlayer =
    asVoicePlayer(this@PlasmoVoiceHolder.voiceInstance)

/**
 * Gets a [VoiceProxyPlayer] of Bungee [ProxiedPlayer].
 *
 * @return The voice player.
 */
context (PlasmoVoiceProxy)
fun ProxiedPlayer.asVoicePlayer(): VoiceProxyPlayer =
    asVoicePlayer(this@PlasmoVoiceProxy)
