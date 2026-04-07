package su.plo.voice.client.audio.source

import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

interface JitterBuffer {

    fun offer(packet: SourceAudioPacket, arrivalTimeMillis: Long)

    fun offer(packet: SourceAudioEndPacket, arrivalTimeMillis: Long)

    fun poll(): PacketWithSequenceNumber?

    fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean = !isEmpty()

    fun reset() {}

    fun clear()

    sealed interface PacketWithSequenceNumber {
        val sequenceNumber: Long
        val arrivalTimeMillis: Long
    }

    data class SourceAudioPacketWrapper(
        val packet: SourceAudioPacket,
        override val arrivalTimeMillis: Long
    ) : PacketWithSequenceNumber {
        override val sequenceNumber: Long
            get() = packet.sequenceNumber
    }

    data class SourceAudioEndPacketWrapper(
        val packet: SourceAudioEndPacket,
        override val arrivalTimeMillis: Long
    ) : PacketWithSequenceNumber {
        override val sequenceNumber: Long
            get() = packet.sequenceNumber
    }

    data class PacketLost(
        override val sequenceNumber: Long,
        override val arrivalTimeMillis: Long
    ) : PacketWithSequenceNumber
}
