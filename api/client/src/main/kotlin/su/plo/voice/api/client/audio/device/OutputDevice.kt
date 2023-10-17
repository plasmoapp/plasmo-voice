package su.plo.voice.api.client.audio.device

import su.plo.voice.api.client.audio.device.source.DeviceSource
import su.plo.voice.api.client.audio.device.source.DeviceSourceParams
import java.util.concurrent.CompletableFuture

/**
 * An audio output device capable of creating and managing audio sources.
 *
 * @param S The type of audio sources that can be created by this output device.
 */
interface OutputDevice<S : DeviceSource> : AudioDevice {

    /**
     * Creates a new audio source associated with this output device.
     *
     * @param stereo If `true`, the created source will be stereo, otherwise - mono.
     * @param params Parameters specific to the device source configuration.
     *
     * @return The device source that has been created.
     *
     * @throws DeviceException If there is an issue creating the audio source.
     */
    @Throws(DeviceException::class)
    fun createSource(stereo: Boolean, params: DeviceSourceParams): S

    /**
     * Closes all audio sources associated with this output device.
     */
    suspend fun closeSources()

    /**
     * Asynchronously closes all audio sources associated with this output device.
     *
     * @return A [CompletableFuture] that represents the completion of the operation.
     */
    fun closeSourcesAsync(): CompletableFuture<Void?>

    override val type: DeviceType
        get() = DeviceType.OUTPUT
}
