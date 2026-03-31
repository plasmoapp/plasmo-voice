package su.plo.voice.server.metrics

import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent
import su.plo.voice.api.server.event.connection.UdpClientDisconnectedEvent
import su.plo.voice.api.server.event.connection.UdpPacketSentEvent

class MetricsListener(
    private val metrics: Metrics,
) {
    @EventSubscribe
    fun onUdpPacketSend(event: UdpPacketSentEvent) {
        metrics.recordPacket(Metrics.PacketDirection.Out, getPacketKind(event.packet), event.packetEncodedSize)
    }

    @EventSubscribe
    fun onUdpClientConnect(event: UdpClientConnectEvent) {
        val connection = event.connection
        val publicIp = connection.connectionAddress?.hostString ?: "unknown"
        metrics.gaugeActivePeer(publicIp, 1)
    }

    @EventSubscribe
    fun onUdpClientDisconnect(event: UdpClientDisconnectedEvent) {
        val connection = event.connection
        val publicIp = connection.connectionAddress?.hostString ?: "unknown"
        metrics.gaugeActivePeer(publicIp, -1)

        if (event.reason == UdpClientDisconnectedEvent.Reason.TIMED_OUT) {
            metrics.hardTimeout(publicIp)
        }
    }
}
