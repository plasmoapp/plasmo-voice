package su.plo.voice.api.client.audio.device

import su.plo.voice.api.client.audio.filter.AudioFilter
import su.plo.voice.api.util.Params
import java.util.*
import java.util.function.Predicate
import javax.sound.sampled.AudioFormat

// todo: doc
interface AudioDevice {
    /**
     * Reloads the device
     */
    @Throws(DeviceException::class)
    fun reload()

    /**
     * Closes the device
     */
    fun close()

    /**
     * @return true if the device is open
     */
    fun isOpen(): Boolean

    /**
     * Adds the filter to device with priority
     */
    fun addFilter(filter: AudioFilter, priority: AudioFilter.Priority)

    /**
     * Adds the filter to device with normal priority
     */
    fun addFilter(filter: AudioFilter) {
        addFilter(filter, AudioFilter.Priority.NORMAL)
    }

    /**
     * Removes the filter from device
     */
    fun removeFilter(filter: AudioFilter)

    /**
     * Gets the device filters
     *
     * @return the device filters
     */
    fun getFilters(): Collection<AudioFilter>

    /**
     * Process all filters
     *
     * @return the processed samples
     */
    fun processFilters(samples: ShortArray, excludeFilter: Predicate<AudioFilter>?): ShortArray

    /**
     * Process all filters
     *
     * @return the processed samples
     */
    fun processFilters(samples: ShortArray): ShortArray {
        return processFilters(samples, null)
    }

    /**
     * Gets the device name
     *
     * @return the device name
     */
    val name: String?

    /**
     * Gets the device audio format
     *
     * @return the audio format with which audio device was open
     */
    val format: AudioFormat

    /**
     * Gets the device buffer size calculated from format
     *
     * @return the buffer size
     */
    val bufferSize: Int

    /**
     * Gets the device type
     *
     * @return the device type
     */
    val type: DeviceType
}
