package su.plo.voice.client.audio.filter;

import lombok.RequiredArgsConstructor;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.AudioUtil;

@RequiredArgsConstructor
public final class StereoToMonoFilter implements AudioFilter {

    private final ConfigEntry<Boolean> activeEntry;

    @Override
    public short[] process(short[] samples) {
        return AudioUtil.convertToMonoShorts(samples);
    }

    @Override
    public boolean isEnabled() {
        return activeEntry.value();
    }
}
