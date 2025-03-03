package su.plo.voice.client.audio.source

import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket

interface JitterBuffer {

    fun offer(packet: SourceAudioPacket)

    fun offer(packet: SourceAudioEndPacket)

    fun poll(): PacketWithSequenceNumber?

    fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean = !isEmpty()

    fun reset() {}

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