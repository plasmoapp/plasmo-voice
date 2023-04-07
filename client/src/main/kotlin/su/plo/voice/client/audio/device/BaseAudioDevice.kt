package su.plo.voice.client.audio.device

import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimaps
import lombok.RequiredArgsConstructor
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.AudioDevice
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.filter.AudioFilter
import java.util.function.Predicate
import javax.sound.sampled.AudioFormat

@RequiredArgsConstructor
abstract class BaseAudioDevice(
    protected val voiceClient: PlasmoVoiceClient,
    override val name: String,
    override val format: AudioFormat
) : AudioDevice {

    override val bufferSize = format.sampleRate.toInt() / 1000 * 20

    private val filters: ListMultimap<AudioFilter.Priority, AudioFilter> = Multimaps.synchronizedListMultimap(
        Multimaps.newListMultimap(HashMap<AudioFilter.Priority, Collection<AudioFilter>>()) { ArrayList() }
    )

    @Throws(DeviceException::class)
    override fun reload() {
        if (!isOpen()) throw DeviceException("Device is not open")

        close()
        open()
    }

    override fun addFilter(filter: AudioFilter, priority: AudioFilter.Priority) {
        // check if filter already exists
        for (filtersPriority in AudioFilter.Priority.values()) {
            val filters: Collection<AudioFilter> = filters[filtersPriority]
            require(!filters.contains(filter)) { "Filter is already exist with priority: $filtersPriority" }
        }
        filters.put(priority, filter)
    }

    override fun removeFilter(filter: AudioFilter) {
        for (filtersPriority in AudioFilter.Priority.values()) {
            val filters: MutableCollection<AudioFilter> = filters[filtersPriority]
            filters.remove(filter)
        }
    }

    override fun getFilters(): Collection<AudioFilter> =
        filters.values()

    override fun processFilters(samples: ShortArray, excludeFilter: Predicate<AudioFilter>?): ShortArray {
        val channels = format.channels

        var samples = samples
        for (filter in filters.values()) {
            if (!filter.isEnabled) continue
            if (filter.supportedChannels > 0 && filter.supportedChannels != channels) continue
            if (excludeFilter?.test(filter) == true) continue

            samples = filter.process(samples)
        }
        return samples
    }

    @Throws(DeviceException::class)
    protected abstract fun open()
}
