package su.plo.voice.api.client.audio.device

import su.plo.voice.api.client.audio.device.source.DeviceSource
import su.plo.voice.api.util.Params
import java.util.concurrent.CompletableFuture

// todo: doc
interface OutputDevice<T : DeviceSource?> : AudioDevice {
    /**
     * Create a new source
     *
     * @param stereo if true source will be stereo
     * @param params parameters
     *
     * @return the device source
     */
    @Throws(DeviceException::class)
    fun createSource(stereo: Boolean, params: Params): T

    /**
     * Closes all sources
     */
    suspend fun closeSources()

    fun closeSourcesAsync(): CompletableFuture<Void?>

    override val type: DeviceType
        get() = DeviceType.OUTPUT
}
