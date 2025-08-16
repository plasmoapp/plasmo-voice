package su.plo.voice.api.server.event.audio.capture

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket

/**
 * Event-counterpart for [ServerActivation.onPlayerActivation].
 * It's fired before [ServerActivation.onPlayerActivation] is invoked.
 */
data class PlayerServerActivationEvent(
    val player: VoicePlayer,
    val activation: ServerActivation,
    val packet: PlayerAudioPacket,
    var result: ServerActivation.Result = ServerActivation.Result.IGNORED,
) : Event
