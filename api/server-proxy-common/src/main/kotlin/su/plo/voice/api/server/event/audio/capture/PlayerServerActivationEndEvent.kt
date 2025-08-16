package su.plo.voice.api.server.event.audio.capture

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket

/**
 * Event-counterpart for [ServerActivation.onPlayerActivationEnd].
 * It's fired before [ServerActivation.onPlayerActivationEnd] is invoked.
 */
data class PlayerServerActivationEndEvent(
    val player: VoicePlayer,
    val activation: ServerActivation,
    val packet: PlayerAudioEndPacket,
    var result: ServerActivation.Result = ServerActivation.Result.IGNORED,
) : Event
