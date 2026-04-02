package su.plo.voice.client.audio.source

import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.concurrent.ConcurrentLinkedQueue

private const val MAX_PENDING_PACKETS = 8

class PendingSourceBuffer(
    private val timeSupplier: TimeSupplier,
    val createdAt: Long = timeSupplier.currentTimeMillis,
) {
    private val packets: ConcurrentLinkedQueue<PendingPacket> = ConcurrentLinkedQueue()

    fun offer(packet: SourceAudioPacket) {
        if (packets.size >= MAX_PENDING_PACKETS) {
            packets.poll()
        }

        packets.offer(PendingAudioPacket(packet, timeSupplier.currentTimeMillis))
    }

    fun offer(packet: SourceAudioEndPacket) {
        if (packets.size >= MAX_PENDING_PACKETS) {
            packets.poll()
        }

        packets.offer(PendingAudioEndPacket(packet, timeSupplier.currentTimeMillis))
    }

    fun drainTo(source: ClientAudioSource<*>, staleThresholdMillis: Long) {
        val now = timeSupplier.currentTimeMillis

        while (true) {
            val pending = packets.poll() ?: break
            if (now - pending.arrivalTime > staleThresholdMillis) continue

            when (pending) {
                is PendingAudioPacket -> source.process(pending.packet)
                is PendingAudioEndPacket -> source.process(pending.packet)
            }
        }
    }
}

sealed interface PendingPacket {
    val arrivalTime: Long
}

data class PendingAudioPacket(
    val packet: SourceAudioPacket,
    override val arrivalTime: Long,
) : PendingPacket

data class PendingAudioEndPacket(
    val packet: SourceAudioEndPacket,
    override val arrivalTime: Long,
) : PendingPacket
