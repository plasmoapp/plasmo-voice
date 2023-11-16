package su.plo.voice.client.audio.filter;

import com.plasmoverse.rnnoise.Denoise;
import com.plasmoverse.rnnoise.DenoiseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.client.audio.filter.AudioFilterContext;
import su.plo.voice.api.util.AudioUtil;

import static su.plo.voice.util.NativesKt.isNativesSupported;

public final class NoiseSuppressionFilter implements AudioFilter {

    private final ConfigEntry<Boolean> activeEntry;
    private final LimiterFilter limiter;

    private @Nullable Denoise instance;

    public NoiseSuppressionFilter(int sampleRate, @NotNull ConfigEntry<Boolean> activeEntry) {
        this.limiter = new LimiterFilter(sampleRate, -6.0F);

        this.activeEntry = activeEntry;

        if (!isNativesSupported()) {
            activeEntry.set(false);
            activeEntry.setDisabled(true);
        } else if (activeEntry.value()) toggle(true);

        activeEntry.clearChangeListeners();
        activeEntry.addChangeListener(this::toggle);
    }

    private void toggle(boolean value) {
        if (value) {
            try {
                this.instance = Denoise.create();
            } catch (Exception | LinkageError e) {
                BaseVoice.LOGGER.error("RNNoise is not available on this platform");
                activeEntry.set(false);
                activeEntry.setDisabled(true);
            }
        } else if (instance != null) {
            instance.close();
            this.instance = null;
        }
    }

    @Override
    public @NotNull String getName() {
        return "noise_suppression";
    }

    @Override
    public short[] process(@NotNull AudioFilterContext context, short[] samples) {
        if (instance == null) return samples;

        limiter.process(context, samples);

        try {
            float[] floats = AudioUtil.shortsToFloats(samples);
            samples = AudioUtil.floatsToShorts(instance.process(floats));
            return samples;
        } catch (DenoiseException e) {
            throw new RuntimeException("Failed to denoise audio samples", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return instance != null && activeEntry.value();
    }

    @Override
    public int getSupportedChannels() {
        return 1;
    }
}
