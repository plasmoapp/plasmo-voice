package su.plo.voice.client.audio.device;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Predicate;

@RequiredArgsConstructor
public abstract class BaseAudioDevice implements AudioDevice {

    protected final PlasmoVoiceClient client;
    protected final @Nullable String name;

    protected AudioFormat format;
    protected Params params;
    @Getter
    protected int bufferSize;

    protected final ListMultimap<AudioFilter.Priority, AudioFilter> filters = Multimaps.synchronizedListMultimap(
            Multimaps.newListMultimap(new HashMap<>(), ArrayList::new)
    );

    @Override
    public void reload() throws DeviceException {
        if (!isOpen()) throw new DeviceException("Device is not open");

        close();
        open(format, params);
    }

    @Override
    public void addFilter(AudioFilter filter, AudioFilter.Priority priority) {
        // check if filter already exists
        for (AudioFilter.Priority filtersPriority : AudioFilter.Priority.values()) {
            Collection<AudioFilter> filters = this.filters.get(filtersPriority);
            if (filters.contains(filter)) {
                throw new IllegalArgumentException("Filter is already exist with priority: " + filtersPriority);
            }
        }

        filters.put(priority, filter);
    }

    @Override
    public void addFilter(AudioFilter filter) {
        addFilter(filter, AudioFilter.Priority.NORMAL);
    }

    @Override
    public void removeFilter(AudioFilter filter) {
        for (AudioFilter.Priority filtersPriority : AudioFilter.Priority.values()) {
            Collection<AudioFilter> filters = this.filters.get(filtersPriority);
            filters.remove(filter);
        }
    }

    @Override
    public Collection<AudioFilter> getFilters() {
        return filters.values();
    }

    @Override
    public short[] processFilters(short[] samples, @Nullable Predicate<AudioFilter> excludeFilter) {
        for (AudioFilter filter : filters.values()) {
            if (filter.isEnabled() && (excludeFilter == null || !excludeFilter.test(filter))) {
                samples = filter.process(samples);
            }
        }

        return samples;
    }
}
