package su.plo.voice.api.server.event.connection

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.socket.UdpConnection

/**
 * This event is fired once a player is disconnected from the UDP server
 * and removed from the [UdpConnectionManager].
 */
class UdpClientDisconnectedEvent(val connection: UdpConnection) : Event
