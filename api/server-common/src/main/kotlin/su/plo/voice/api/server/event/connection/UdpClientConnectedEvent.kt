package su.plo.voice.api.server.event.connection

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.socket.UdpConnection

/**
 * This event is fired once the player is successfully connected to the UDP server
 * and added to [UdpConnectionManager]
 */
class UdpClientConnectedEvent(val connection: UdpConnection) : Event
