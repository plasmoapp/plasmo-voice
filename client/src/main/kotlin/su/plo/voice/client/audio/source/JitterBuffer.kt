package su.plo.voice.client.audio.source

import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue

class JitterBuffer(
    private val packetDelay: Int
) {

    private val queue: Queue<PacketWithSequenceNumber> = if (packetDelay <= 1) {
        LinkedBlockingQueue()
    } else {
        PriorityBlockingQueue(
            packetDelay * 2,
            compareBy { it.sequenceNumber }
        )
    }

    private var endPacket: SourceAudioEndPacket? = null

    fun offer(packet: SourceAudioPacket) {
        if (endPacket != null && packet.sequenceNumber > endPacket!!.sequenceNumber) {
            this.endPacket = null
        }

        queue.offer(SourceAudioPacketWrapper(packet))
    }

    fun offer(packet: SourceAudioEndPacket) {
        this.endPacket = packet

        queue.offer(SourceAudioEndPacketWrapper(packet))
    }

    fun poll(): PacketWithSequenceNumber? {
        if (endPacket != null || queue.size >= packetDelay) {
            return queue.poll()
        }

        return null
    }

    sealed interface PacketWithSequenceNumber {
        val sequenceNumber: Long
    }

    data class SourceAudioPacketWrapper(
        val packet: SourceAudioPacket
    ) : PacketWithSequenceNumber {
        override val sequenceNumber: Long
            get() = packet.sequenceNumber
    }

    data class SourceAudioEndPacketWrapper(
        val packet: SourceAudioEndPacket
    ) : PacketWithSequenceNumber {
        override val sequenceNumber: Long
            get() = packet.sequenceNumber
    }
}
