package su.plo.voice.api.server.event.audio.capture

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.audio.capture.ServerActivation
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket
import kotlin.jvm.internal.DefaultConstructorMarker

/**
 * Event-counterpart for [ServerActivation.onPlayerActivationEnd].
 * It's fired before [ServerActivation.onPlayerActivationEnd] is invoked.
 */
data class PlayerServerActivationEndEvent(
    val player: VoicePlayer,
    val activation: ServerActivation,
    val packet: PlayerAudioEndPacket,
    var result: ServerActivation.Result,
) : Event {

    constructor(
        player: VoicePlayer,
        activation: ServerActivation,
        packet: PlayerAudioEndPacket
    ) : this(player, activation, packet, ServerActivation.Result.IGNORED)

    @Deprecated("Binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        player: VoicePlayer,
        activation: ServerActivation,
        packet: PlayerAudioEndPacket,
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
