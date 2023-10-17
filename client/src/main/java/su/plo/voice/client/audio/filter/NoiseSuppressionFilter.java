package su.plo.voice.client.audio.filter;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.rnnoise.Denoiser;

public final class NoiseSuppressionFilter implements AudioFilter {

    private final ConfigEntry<Boolean> activeEntry;
    private final ThreadLocal<Denoiser> instance = new ThreadLocal<>();
    private final ThreadLocal<LimiterFilter> limiter;

    public NoiseSuppressionFilter(int sampleRate, @NotNull ConfigEntry<Boolean> activeEntry) {
        this.limiter = ThreadLocal.withInitial(() -> new LimiterFilter(sampleRate, -6.0F));

        this.activeEntry = activeEntry;
        if (activeEntry.value()) toggle(true);
        activeEntry.clearChangeListeners();
        activeEntry.addChangeListener(this::toggle);
    }

    private void toggle(boolean value) {
        if (value) {
            try {
                instance.set(new Denoiser());
            } catch (Exception e) {
                BaseVoice.LOGGER.error("RNNoise is not available on this platform");
                activeEntry.set(false);
            }
        } else if (instance.get() != null) {
            instance.get().close();
            instance.set(null);
        }
    }

    @Override
    public @NotNull String getName() {
        return "noise_suppression";
    }

    @Override
    public short[] process(short[] samples) {
        limiter.get().process(samples);

        float[] floats = AudioUtil.shortsToFloats(samples);
        samples = AudioUtil.floatsToShorts(instance.get().process(floats));
        return samples;
    }

    @Override
    public boolean isEnabled() {
        return instance != null && activeEntry.value();
    }
}
