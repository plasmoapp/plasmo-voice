package su.plo.voice.api.server.audio.source

import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.provider.AudioFrameProvider
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*
import java.util.function.Supplier

/**
 * Represents a proximity audio source.
 */
interface ServerProximitySource<S : SourceInfo> : ServerAudioSource<S> {

    /**
     * Gets the position of this audio source.
     *
     * @return The position.
     */
    val position: ServerPos3d

    /**
     * Gets or sets the angle of this audio source.
     *
     * @return The angle of the audio source.
     */
    var angle: Int

    /**
     * Sends an audio end packet.
     *
     * @param sequenceNumber The sequence number of the frame.
     * @param distance The maximum distance at which players can receive the packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioEnd(sequenceNumber: Long, distance: Short): Boolean =
        sendPacket(SourceAudioEndPacket(id, sequenceNumber), distance)

    /**
     * Sends an encoded and encrypted audio frame to players within the specified distance with null activation ID.
     *
     * @param frame      The frame to send.
     * @param sequenceNumber The sequence number of the frame.
     * @param distance   The maximum distance at which players can hear the audio.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioFrame(frame: ByteArray, sequenceNumber: Long, distance: Short): Boolean =
        sendAudioFrame(frame, sequenceNumber, distance, null)

    /**
     * Sends an encoded and encrypted audio frame to players within the specified distance with an activation ID.
     *
     * @param frame      The frame to send.
     * @param sequenceNumber The sequence number of the frame.
     * @param distance   The maximum distance at which players can hear the audio.
     * @param activationId The activation ID for the audio packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioFrame(frame: ByteArray, sequenceNumber: Long, distance: Short, activationId: UUID?): Boolean {
        val audioPacket = SourceAudioPacket(
            sequenceNumber,
            state.toByte(),
            frame,
            id,
            distance
        )

        return sendAudioPacket(audioPacket, distance, activationId)
    }

    /**
     * Sends an audio packet to players within the specified distance with null activation ID.
     *
     * @param packet     The audio packet to send.
     * @param distance   The maximum distance at which players can hear the audio.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket, distance: Short): Boolean =
        sendAudioPacket(packet, distance, null)

    /**
     * Sends an audio packet to players within the specified distance with an activation ID.
     *
     * @param packet       The audio packet to send.
     * @param distance     The maximum distance at which players can hear the audio.
     * @param activationId The activation ID for the audio packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket, distance: Short, activationId: UUID?): Boolean

    /**
     * Sends a TCP packet to players within the specified distance.
     *
     * @param packet   The packet to send.
     * @param distance The maximum distance at which players can receive the packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendPacket(packet: Packet<*>, distance: Short): Boolean

    /**
     * Creates a new audio sender for this source.
     *
     * @param frameProvider The audio frame provider.
     * @param distanceProvider The provider of the maximum distance at which players can hear the audio.
     * @return An audio sender.
     */
    fun createAudioSender(frameProvider: AudioFrameProvider, distanceProvider: Supplier<Short>): AudioSender

    /**
     * Creates a new audio sender for this source with fixed distance.
     *
     * @param frameProvider The audio frame provider.
     * @param distance The maximum distance at which players can hear the audio.
     * @return An audio sender.
     */
    fun createAudioSender(frameProvider: AudioFrameProvider, distance: Short): AudioSender =
        createAudioSender(frameProvider) { distance }

    /**
     * Gets the server source line to which this audio source belongs.
     *
     * @return The source line.
     */
    override fun getLine(): ServerSourceLine
}
