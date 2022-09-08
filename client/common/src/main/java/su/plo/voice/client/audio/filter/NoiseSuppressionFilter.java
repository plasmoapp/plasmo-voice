package su.plo.voice.client.audio.filter;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.rnnoise.Denoiser;

public final class NoiseSuppressionFilter extends LimiterFilter {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ConfigEntry<Boolean> activeEntry;

    private Denoiser instance;

    public NoiseSuppressionFilter(int sampleRate, @NotNull ConfigEntry<Boolean> activeEntry) {
        super(sampleRate, -6.0F, null, null);

        this.activeEntry = activeEntry;
        if (activeEntry.value()) toggle();
        activeEntry.onUpdate((active) -> toggle());
    }

    private void toggle() {
        if (activeEntry.value()) {
            try {
                instance = new Denoiser();
            } catch (Exception e) {
                LOGGER.error("RNNoise is not available on this platform");
                activeEntry.set(false);
            }
        } else if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    @Override
    public short[] process(short[] samples) {
        super.process(samples);

        float[] floats = AudioUtil.shortsToFloats(samples);
        samples = AudioUtil.floatsToShorts(instance.process(floats));
        return samples;
    }

    @Override
    public boolean isEnabled() {
        return instance != null && activeEntry.value();
    }
}
