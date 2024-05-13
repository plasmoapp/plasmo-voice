package su.plo.voice.api.server.audio.source

import su.plo.slib.api.position.Pos3d
import su.plo.voice.api.server.audio.capture.PlayerActivationInfo
import su.plo.voice.api.server.audio.provider.AudioFrameProvider
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.source.DirectSourceInfo
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*

/**
 * Base interface for a direct sources.
 */
interface BaseServerDirectSource : ServerAudioSource<DirectSourceInfo> {

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
     * Sends an audio end packet.
     *
     * @param sequenceNumber The sequence number of the frame.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioEnd(sequenceNumber: Long): Boolean =
        sendPacket(SourceAudioEndPacket(id, sequenceNumber))

    /**
     * Sends an encoded and encrypted audio frame to players with null activation info.
     *
     * @param frame      The frame to send.
     * @param sequenceNumber The sequence number of the frame.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioFrame(frame: ByteArray, sequenceNumber: Long): Boolean =
        sendAudioFrame(frame, sequenceNumber, null)

    /**
     * Sends an encoded and encrypted audio frame to players with an activation info.
     *
     * @param frame         The frame to send.
     * @param sequenceNumber    The sequence number of the frame.
     * @param activationInfo    The activation info for the audio packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioFrame(frame: ByteArray, sequenceNumber: Long, activationInfo: PlayerActivationInfo?): Boolean {
        val audioPacket = SourceAudioPacket(
            sequenceNumber,
            state.toByte(),
            frame,
            id,
            0
        )

        return sendAudioPacket(audioPacket, activationInfo)
    }

    /**
     * Sends an audio packet to players with null activation info.
     *
     * @param packet     The audio packet to send.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket): Boolean {
        return sendAudioPacket(packet, null)
    }

    /**
     * Sends an audio packet to players with activation info.
     *
     * @param packet            The audio packet to send.
     * @param activationInfo    The activation info for the audio packet.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendAudioPacket(packet: SourceAudioPacket, activationInfo: PlayerActivationInfo?): Boolean

    /**
     * Sends a TCP packet to players.
     *
     * @param packet   The packet to send.
     * @return `true` if the packet was successfully sent, `false` otherwise.
     */
    fun sendPacket(packet: Packet<*>): Boolean

    /**
     * Creates a new audio sender for this source.
     *
     * @param frameProvider The audio frame provider.
     * @return An audio sender.
     */
    fun createAudioSender(frameProvider: AudioFrameProvider): AudioSender
}
