package su.plo.voice.api.client.audio.device.source

import su.plo.voice.api.client.audio.device.AudioDevice
import java.util.concurrent.CompletableFuture

/**
 * Represents an device audio source.
 */
interface DeviceSource {

    /**
     * Gets the associated [AudioDevice] for this source.
     */
    val device: AudioDevice

    /**
     * Writes audio samples to the source.
     *
     * @param samples The audio samples to write.
     * @param applyFilters Whether the audio filters from [device] should be applied.
     */
    fun write(samples: ShortArray, applyFilters: Boolean = true)

    /**
     * Writes audio samples to the source without applying [device] filters.
     *
     * @param samples The audio samples to write.
     */
    fun write(samples: ByteArray)

    /**
     * Closes the audio source.
     */
    @JvmSynthetic
    suspend fun close()

    /**
     * Closes the audio source asynchronously.
     *
     * @return A [CompletableFuture] that completes when the source is closed.
     */
    fun closeAsync(): CompletableFuture<Void?>

    /**
     * Checks if the audio source is closed.
     *
     * @return `true` if the source is closed, `false` otherwise.
     */
    fun isClosed(): Boolean
}

