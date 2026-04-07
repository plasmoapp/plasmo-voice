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

        packets.offer(PendingAudioPacket(packet, timeSupplier.nanoTime))
    }

    fun offer(packet: SourceAudioEndPacket) {
        if (packets.size >= MAX_PENDING_PACKETS) {
            packets.poll()
        }

        packets.offer(PendingAudioEndPacket(packet, timeSupplier.nanoTime))
    }

    fun drainTo(source: ClientAudioSource<*>) {
        while (true) {
            val pending = packets.poll() ?: break

            when (pending) {
                is PendingAudioPacket -> source.process(pending.packet, pending.arrivalTimeNanos)
                is PendingAudioEndPacket -> source.process(pending.packet, pending.arrivalTimeNanos)
            }
        }
    }
}

sealed interface PendingPacket {
    val arrivalTimeNanos: Long
}

data class PendingAudioPacket(
    val packet: SourceAudioPacket,
    override val arrivalTimeNanos: Long,
) : PendingPacket

data class PendingAudioEndPacket(
    val packet: SourceAudioEndPacket,
    override val arrivalTimeNanos: Long,
) : PendingPacket
