package su.plo.voice.api.server.event.connection

import su.plo.voice.api.event.EventCancellableBase
import su.plo.voice.api.server.connection.UdpConnectionManager
import su.plo.voice.api.server.socket.UdpConnection

/**
 * This event is fired once the player is successfully connected to the UDP server,
 * but not added to [UdpConnectionManager] yet
 */
class UdpClientConnectEvent(val connection: UdpConnection) : EventCancellableBase()
