package su.plo.voice.api.client.audio.source

import su.plo.voice.api.audio.source.AudioSource
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.proto.data.audio.source.SourceInfo
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Represents a client-side audio source used for playing audio.
 */
interface ClientAudioSource<S : SourceInfo> : AudioSource<S> {

    /**
     * Gets or sets the timeout duration in milliseconds for closing this audio source.
     * If the source remains inactive for this duration, it will be automatically closed.
     *
     * Set to 0 to disable.
     *
     * Default is 500ms.
     *
     * @return The close timeout in ms.
     */
    var closeTimeoutMs: Long

    /**
     * Gets or sets the source group to which this audio source belongs.
     *
     * @return The source group.
     */
    val source: AlSource

    /**
     * Updates the audio source with new source information.
     *
     * @param sourceInfo The new source information to update.
     *
     * @throws DeviceException if an error occurs while updating the audio source.
     */
    @Throws(DeviceException::class)
    fun update(sourceInfo: S)

    /**
     * Updates the audio source with new source information, performing unchecked type casting.
     *
     * @param sourceInfo The new source information to update.
     *
     * @throws DeviceException if an error occurs while updating the audio source.
     */
    @Throws(DeviceException::class)
    @Suppress("UNCHECKED_CAST")
    fun updateUnchecked(sourceInfo: SourceInfo) =
        update(sourceInfo as S)

    /**
     * Processes an incoming audio packet and plays the audio.
     *
     * @param packet The audio packet to process.
     */
    fun process(packet: SourceAudioPacket)

    /**
     * Processes an audio end packet indicating the end of audio playback.
     *
     * @param packet The audio end packet to process.
     */
    fun process(packet: SourceAudioEndPacket)

    /**
     * Checks if the audio source is closed.
     *
     * @return `true` if the audio source is closed, `false` otherwise.
     */
    fun isClosed(): Boolean

    /**
     * Checks if the audio source is currently playing audio.
     *
     * @return `true` if the audio source is playing audio, `false` otherwise.
     */
    fun isActivated(): Boolean

    /**
     * Checks if the audio source can be heard, meaning it is within reach and activated.
     *
     * @return `true` if the audio source can be heard, `false` otherwise.
     */
    fun canHear(): Boolean

    /**
     * Closes the audio source.
     */
    @JvmSynthetic
    suspend fun close()

    /**
     * Closes the audio source asynchronously.
     *
     * @return A [CompletableFuture] indicating the completion of the close operation.
     */
    fun closeAsync(): CompletableFuture<Void?>
}
