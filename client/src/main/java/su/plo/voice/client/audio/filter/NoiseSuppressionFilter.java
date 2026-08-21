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
    private static final int STEREO = 2;

    private final ConfigEntry<Boolean> activeEntry;
    private final LimiterFilter limiter;

    private @Nullable Denoise instance;
    private @Nullable Denoise rightInstance;

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
                this.rightInstance = Denoise.create();
            } catch (Exception | LinkageError e) {
                BaseVoice.LOGGER.error("RNNoise is not available on this platform", e);
                close();
                activeEntry.set(false);
                activeEntry.setDisabled(true);
            }
        } else {
            close();
        }
    }

    private void close() {
        if (instance != null) {
            instance.close();
            this.instance = null;
        }

        if (rightInstance != null) {
            rightInstance.close();
            this.rightInstance = null;
        }
    }

    @Override
    public @NotNull String getName() {
        return "noise_suppression";
    }

    @Override
    public short[] process(@NotNull AudioFilterContext context, short[] samples) {
        Denoise left = instance;
        Denoise right = rightInstance;
        if (left == null || right == null) return samples;

        int channels = context.getChannels();
        if (channels != 1 && channels != STEREO) return samples;

        limiter.process(context, samples);

        try {
            if (channels == 1) return denoise(left, samples);

            return interleave(
                    denoise(left, channel(samples, 0)),
                    denoise(right, channel(samples, 1))
            );
        } catch (DenoiseException e) {
            throw new RuntimeException("Failed to denoise audio samples", e);
        }
    }

    private static short[] denoise(@NotNull Denoise denoise, short[] samples) throws DenoiseException {
        return AudioUtil.floatsToShorts(denoise.process(AudioUtil.shortsToFloats(samples)));
    }

    // One channel of an interleaved stereo buffer, on its own
    private static short[] channel(short[] samples, int offset) {
        short[] channel = new short[samples.length / STEREO];
        for (int i = 0; i < channel.length; i++) {
            channel[i] = samples[i * STEREO + offset];
        }

        return channel;
    }

    private static short[] interleave(short[] left, short[] right) {
        short[] samples = new short[left.length + right.length];
        for (int i = 0; i < left.length && i < right.length; i++) {
            samples[i * STEREO] = left[i];
            samples[i * STEREO + 1] = right[i];
        }

        return samples;
    }

    @Override
    public boolean isEnabled() {
        return instance != null && activeEntry.value();
    }
}
