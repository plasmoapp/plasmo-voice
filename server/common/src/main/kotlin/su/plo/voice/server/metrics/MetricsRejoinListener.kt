package su.plo.voice.server.metrics

import com.google.common.collect.Sets
import su.plo.slib.api.entity.player.McPlayer
import su.plo.slib.api.event.player.McPlayerQuitEvent
import su.plo.voice.api.event.EventPriority
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.api.server.event.connection.TcpPacketSendEvent
import su.plo.voice.api.server.event.connection.UdpClientConnectEvent
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket
import java.util.UUID

class MetricsRejoinListener(
    private val metrics: Metrics,
) {

    private val connectedPrior = Sets.newConcurrentHashSet<UUID>()

    init {
        McPlayerQuitEvent.registerListener(this::onPlayerQuit)
    }

    fun unregister() {
        McPlayerQuitEvent.unregisterListener(this::onPlayerQuit)
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    fun onPacketSend(event: TcpPacketSendEvent) {
        val packet = event.packet as? ConnectionPacket ?: return

        if (!connectedPrior.contains(event.player.instance.uuid)) return

        metrics.rejoinAttempt(packet.ip)
    }

    @EventSubscribe
    fun onUdpClientConnect(event: UdpClientConnectEvent) {
        val player = event.connection.player
        if (connectedPrior.add(player.instance.uuid)) return

        val connection = event.connection
        val publicIp = connection.connectionAddress?.hostString ?: "unknown"
        metrics.rejoinSuccess(publicIp)
    }

    private fun onPlayerQuit(player: McPlayer) {
        connectedPrior.remove(player.uuid)
    }
}
