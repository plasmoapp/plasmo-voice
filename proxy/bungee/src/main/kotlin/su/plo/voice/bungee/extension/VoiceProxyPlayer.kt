package su.plo.voice.bungee.extension

import net.md_5.bungee.api.connection.ProxiedPlayer
import su.plo.voice.api.proxy.player.VoiceProxyPlayer

/**
 * Gets a Velocity [ProxiedPlayer] of [VoiceProxyPlayer].
 *
 * @return The Velocity player.
 */
fun VoiceProxyPlayer.asBungeePlayer(): ProxiedPlayer =
    instance.getInstance()
