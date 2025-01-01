package su.plo.voice.api.proxy.event.connection

import su.plo.voice.api.event.EventCancellableBase
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.packets.Packet

/**
 * This event is fired once the server has received a packet from the player but has not yet handled it.
 *
 * If event is cancelled, packet won't be forwarded to the backend server.
 */
data class TcpPacketReceivedEvent(
    val player: VoicePlayer,
    val packet: Packet<*>
) : EventCancellableBase()