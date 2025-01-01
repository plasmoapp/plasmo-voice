package su.plo.voice.api.proxy.event.connection

import su.plo.voice.api.event.EventCancellableBase
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.Packet

/**
 * This event is fired when the server is about to send a [Packet] to the player.
 *
 * This event is fired ONLY when invoked by [VoicePlayer.sendPacket].
 * If it's forwarded from the server (server -> proxy -> player) this event won't be fired.
 */
data class TcpPacketSendEvent(
    val player: VoicePlayer,
    val packet: Packet<*>
) : EventCancellableBase()