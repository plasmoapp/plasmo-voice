package su.plo.voice.client.audio.source

import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue

class StaticJitterBuffer(
    private val timeSupplier: TimeSupplier,
    private val packetDelay: Int,
    private val staleThresholdMillis: Long = 500,
) : JitterBuffer {

    private val queue: Queue<JitterBuffer.PacketWithSequenceNumber> = if (packetDelay <= 1) {
        LinkedBlockingQueue()
    } else {
        PriorityBlockingQueue(
            packetDelay * 2,
            compareBy { it.sequenceNumber }
        )
    }

    private var endPacket: SourceAudioEndPacket? = null

    override fun offer(packet: SourceAudioPacket) {
        if (endPacket != null && packet.sequenceNumber > endPacket!!.sequenceNumber) {
            this.endPacket = null
        }

        queue.offer(JitterBuffer.SourceAudioPacketWrapper(packet, timeSupplier.currentTimeMillis))
    }

    override fun offer(packet: SourceAudioEndPacket) {
        this.endPacket = packet

        queue.offer(JitterBuffer.SourceAudioEndPacketWrapper(packet, timeSupplier.currentTimeMillis))
    }

    override fun poll(): JitterBuffer.PacketWithSequenceNumber? {
        if (endPacket != null || queue.size >= packetDelay) {
            return queue.poll()
                ?.takeIf { timeSupplier.currentTimeMillis - it.arrivalTime < staleThresholdMillis }
        }

        return null
    }

    override fun isEmpty(): Boolean =
        queue.isEmpty()
}
