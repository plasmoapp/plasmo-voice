package su.plo.voice.api.server.audio.source

import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

/**
 * Represents an audio source with position.
 */
interface ServerPositionalSource<S : SourceInfo> : ServerAudioSource<S> {

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
     * Sends an audio packet to players within the specified distance with null activation ID.
     *
     * @param packet     The audio packet to send.
     * @param distance   The maximum distance at which players can hear the audio.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket, distance: Short): Boolean {
        return sendAudioPacket(packet, distance, null)
    }

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
     * Gets the server source line to which this audio source belongs.
     *
     * @return The source line.
     */
    override fun getLine(): ServerSourceLine
}
