package su.plo.voice.client.audio.filter;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.client.audio.filter.AudioFilterContext;
import su.plo.voice.api.util.AudioUtil;

@RequiredArgsConstructor
public final class StereoToMonoFilter implements AudioFilter {

    private final ConfigEntry<Boolean> activeEntry;

    @Override
    public @NotNull String getName() {
        return "stereo_to_mono";
    }

    @Override
    public short[] process(@NotNull AudioFilterContext context, short[] samples) {
        context.setChannels(1);

        return AudioUtil.convertToMonoShorts(samples);
    }

    @Override
    public boolean isEnabled() {
        return activeEntry.value();
    }

    @Override
    public int getSupportedChannels() {
        return 2;
    }
}
