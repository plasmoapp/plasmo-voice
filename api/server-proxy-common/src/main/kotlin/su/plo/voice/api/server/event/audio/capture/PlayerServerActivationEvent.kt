package su.plo.voice.api.server.event.audio.capture

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket
import kotlin.jvm.internal.DefaultConstructorMarker

/**
 * Event-counterpart for [ServerActivation.onPlayerActivation].
 * It's fired before [ServerActivation.onPlayerActivation] is invoked.
 */
data class PlayerServerActivationEvent(
    val player: VoicePlayer,
    val activation: ServerActivation,
    val packet: PlayerAudioPacket,
    var result: ServerActivation.Result,
) : Event {

    constructor(
        player: VoicePlayer,
        activation: ServerActivation,
        packet: PlayerAudioPacket
    ) : this(player, activation, packet, ServerActivation.Result.IGNORED)

    @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        player: VoicePlayer,
        activation: ServerActivation,
        packet: PlayerAudioPacket,
        result: ServerActivation.Result?,
        defaultsMask: Int,
        marker: DefaultConstructorMarker?
    ) : this(
        player,
        activation,
        packet,
        if (defaultsMask and 0x8 != 0) ServerActivation.Result.IGNORED else result!!
    )
}
