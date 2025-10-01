package su.plo.voice.api.server.event.audio.capture

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.player.VoicePlayer

/**
 * Event-counterpart for [ServerActivation.onPlayerActivationStart].
 * It's fired before [ServerActivation.onPlayerActivationStart] is invoked.
 */
data class PlayerServerActivationStartEvent(
    val player: VoicePlayer,
    val activation: ServerActivation,
) : Event
