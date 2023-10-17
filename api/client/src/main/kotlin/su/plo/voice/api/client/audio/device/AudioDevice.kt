package su.plo.voice.api.client.audio.device

import su.plo.voice.api.client.audio.filter.AudioFilter
import java.util.*
import java.util.function.Predicate
import javax.sound.sampled.AudioFormat

/**
 * Represents an audio device used for audio input or output.
 */
interface AudioDevice {

    /**
     * Gets the name of the audio device.
     *
     * @return The name of the device.
     */
    val name: String

    /**
     * Gets the audio format associated with the device.
     *
     * @return The audio format with which the audio device was opened.
     */
    val format: AudioFormat

    /**
     * Gets the 20ms frame size calculated based on the audio format.
     *
     * @return The frame size for the device.
     */
    val frameSize: Int

    /**
     * Gets the type of the audio device.
     *
     * @return The type of the device.
     */
    val type: DeviceType

    /**
     * Reloads (closes and opens) the audio device.
     *
     * @throws DeviceException if there is an issue reloading the device.
     */
    @Throws(DeviceException::class)
    fun reload()

    /**
     * Closes the audio device.
     */
    fun close()

    /**
     * Checks if the audio device is open.
     *
     * @return `true` if the device is open, `false` otherwise.
     */
    fun isOpen(): Boolean

    /**
     * Adds an audio filter to the device with the specified priority.
     *
     * @param filter   The audio filter to add.
     * @param priority The priority of the filter.
     */
    fun addFilter(filter: AudioFilter, priority: AudioFilter.Priority)

    /**
     * Adds an audio filter to the device with normal priority.
     *
     * @param filter The audio filter to add.
     */
    fun addFilter(filter: AudioFilter) {
        addFilter(filter, AudioFilter.Priority.NORMAL)
    }

    /**
     * Removes an audio filter from the device.
     *
     * @param filter The audio filter to remove.
     */
    fun removeFilter(filter: AudioFilter)

    /**
     * Gets the collection of audio filters applied to the device.
     *
     * @return The collection of audio filters.
     */
    fun getFilters(): Collection<AudioFilter>

    /**
     * Processes the provided audio samples through all applicable filters.
     *
     * @param samples       The input audio samples to process.
     * @param excludeFilter A filter predicate to exclude specific filters from processing.
     * @return The processed audio samples.
     */
    fun processFilters(samples: ShortArray, excludeFilter: Predicate<AudioFilter>?): ShortArray

    /**
     * Processes the provided audio samples through all applicable filters.
     *
     * @param samples The input audio samples to process.
     * @return The processed audio samples.
     */
    fun processFilters(samples: ShortArray): ShortArray {
        return processFilters(samples, null)
    }
}
