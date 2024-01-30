package su.plo.voice.velocity.extension

import com.velocitypowered.api.proxy.Player
import su.plo.voice.api.proxy.player.VoiceProxyPlayer

/**
 * Gets a Velocity [Player] of [VoiceProxyPlayer].
 *
 * @return The Velocity player.
 */
fun VoiceProxyPlayer.asVelocityPlayer(): Player =
    instance.getInstance()
