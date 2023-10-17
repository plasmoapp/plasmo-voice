package su.plo.voice.api.server.audio.source

import su.plo.slib.api.position.Pos3d
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

/**
 * Base interface for a direct sources.
 */
interface BaseServerDirectSource : ServerAudioSource<DirectSourceInfo?> {

    /**
     * Gets or sets the direct source sender.
     *
     * Sender is displayed in the player overlay.
     *
     * @return The source sender.
     */
    var sender: VoicePlayer?

    /**
     * Gets or sets the relative position of the source.
     *
     * @return The source relative position.
     */
    var relativePosition: Pos3d?

    /**
     * Gets or sets the look angle of the source.
     *
     * Only works if [relativePosition] is set.
     *
     * @return The look angle.
     */
    var lookAngle: Pos3d?

    /**
     * Gets or sets whether the source position should be camera relative.
     *
     * If `true`, `AL_SOURCE_RELATIVE` mode will be used to play the audio;
     * otherwise absolute position of the player will be used to play the audio.
     *
     * `true` by default.
     *
     * @return Whether the source position is camera relative.
     */
    var isCameraRelative: Boolean

    /**
     * Sends an audio packet to players with an activation ID.
     *
     * @param packet       The audio packet to send.
     * @param activationId The activation ID for the audio packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket, activationId: UUID?): Boolean

    /**
     * Sends an audio packet to players with null activation ID.
     *
     * @param packet     The audio packet to send.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket): Boolean {
        return sendAudioPacket(packet, null)
    }

    /**
     * Sends a TCP packet to players.
     *
     * @param packet   The packet to send.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendPacket(packet: Packet<*>): Boolean
}
