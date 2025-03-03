package su.plo.voice.client.audio.source

import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.PriorityBlockingQueue

class StaticJitterBuffer(
    private val packetDelay: Int
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

        queue.offer(JitterBuffer.SourceAudioPacketWrapper(packet))
    }

    override fun offer(packet: SourceAudioEndPacket) {
        this.endPacket = packet

        queue.offer(JitterBuffer.SourceAudioEndPacketWrapper(packet))
    }

    override fun poll(): JitterBuffer.PacketWithSequenceNumber? {
        if (endPacket != null || queue.size >= packetDelay) {
            return queue.poll()
        }

        return null
    }

    override fun isEmpty(): Boolean =
        queue.isEmpty()
}
