package su.plo.voice.client.audio.device;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.filter.AudioFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public abstract class BaseAudioDevice implements AudioDevice {
    private final ListMultimap<AudioFilter.Priority, AudioFilter> filters = Multimaps.synchronizedListMultimap(
            Multimaps.newListMultimap(new HashMap<>(), ArrayList::new)
    );

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
    public short[] processFilters(short[] samples) {
        for (AudioFilter filter : filters.values()) {
            if (filter.isEnabled()) {
                samples = filter.process(samples);
            }
        }

        return samples;
    }
}
