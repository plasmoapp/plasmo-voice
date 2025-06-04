package su.plo.voice.client.audio.source

import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue
import kotlin.math.abs
import kotlin.math.round

class AdaptiveJitterBuffer(
    private val timeSupplier: TimeSupplier,
    packetDelay: Int
) : JitterBuffer {

    private val packetDelayMillis = packetDelay * 20L

    private val queue: Queue<PacketWithArrivalTime> = if (packetDelay <= 1) {
        LinkedBlockingQueue()
    } else {
        PriorityBlockingQueue(
            packetDelay * 2,
            compareBy { it.packet.sequenceNumber }
        )
    }

    private var endPacket: SourceAudioEndPacket? = null

    private var firstPacketArrival: Long? = null
    private var firstSequenceNumber: Long? = null

    private var lastPacketArrival: Long? = null
    private var jitterEstimate: Double = 0.0
    private var adaptiveDelayMillis: Long = packetDelayMillis

    override fun offer(packet: SourceAudioPacket) {
        if (endPacket != null && packet.sequenceNumber > endPacket!!.sequenceNumber) {
            endPacket = null
            firstPacketArrival = null
            firstSequenceNumber = null
            lastPacketArrival = null
        }

        val arrivalTime = timeSupplier.currentTimeMillis

        queue.offer(
            PacketWithArrivalTime(
                JitterBuffer.SourceAudioPacketWrapper(packet, arrivalTime),
                scheduledPlaybackTime(packet.sequenceNumber, arrivalTime)
            )
        )
    }

    override fun offer(packet: SourceAudioEndPacket) {
        endPacket = packet

        val arrivalTime = timeSupplier.currentTimeMillis

        queue.offer(
            PacketWithArrivalTime(
                JitterBuffer.SourceAudioEndPacketWrapper(packet, arrivalTime),
                scheduledPlaybackTime(packet.sequenceNumber, arrivalTime)
            )
        )
    }

    private fun scheduledPlaybackTime(sequenceNumber: Long, arrivalTime: Long): Long {
        lastPacketArrival?.let { last ->
            val transit = arrivalTime - last
            // I don't really want to get sender's timestamp here
            // so let's assume that packets are sent at 20 rate without fluctuations
            val delta = abs(transit - 20)
            jitterEstimate += (delta - jitterEstimate) / 16.0
            adaptiveDelayMillis = (round(jitterEstimate / 20.0) * 20).toLong()
        }
        lastPacketArrival = arrivalTime

        if (firstSequenceNumber == null) {
            firstPacketArrival = arrivalTime
            firstSequenceNumber = sequenceNumber
        }

        val sequenceOffset = sequenceNumber - (firstSequenceNumber ?: sequenceNumber)
        val scheduledPlaybackTime = (firstPacketArrival ?: arrivalTime) + packetDelayMillis + sequenceOffset * 20

        return scheduledPlaybackTime
    }

    override fun poll(): JitterBuffer.PacketWithSequenceNumber? {
        val next = queue.peek() ?: return null

        if (timeSupplier.currentTimeMillis >= next.scheduledPlaybackTime + adaptiveDelayMillis) {
            return queue.poll().packet
        }

        return null
    }

    override fun isEmpty(): Boolean =
        queue.isEmpty()

    override fun reset() {
        firstSequenceNumber = null
        firstPacketArrival = null
    }

    data class PacketWithArrivalTime(
        val packet: JitterBuffer.PacketWithSequenceNumber,
        val scheduledPlaybackTime: Long
    )
}
