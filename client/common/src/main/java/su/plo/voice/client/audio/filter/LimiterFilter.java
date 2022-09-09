package su.plo.voice.client.audio.filter;

import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.config.entry.IntConfigEntry;

public class LimiterFilter implements AudioFilter {

    private static final float SLOPE = 1F;
    private static final float OUTPUT_GAIN = AudioUtil.dbToMul(0F);
    private static final float DEFAULT_THRESHOLD = -6F;

    private final int sampleRate;
    private final Float threshold;
    private final @Nullable ConfigEntry<Boolean> activeEntry;
    private final @Nullable IntConfigEntry thresholdEntry;
    private float[] envelopeBuf = new float[0];
    private float envelope;

    public LimiterFilter(int sampleRate,
                         Float threshold) {
        this(sampleRate, threshold, null, null);
    }

    public LimiterFilter(int sampleRate,
                         @Nullable ConfigEntry<Boolean> activeEntry,
                         @Nullable IntConfigEntry thresholdEntry) {
        this(sampleRate, null, activeEntry, thresholdEntry);
    }

    private LimiterFilter(int sampleRate,
                         Float threshold,
                         @Nullable ConfigEntry<Boolean> activeEntry,
                         @Nullable IntConfigEntry thresholdEntry) {
        this.sampleRate = sampleRate;
        this.threshold = threshold;
        this.activeEntry = activeEntry;
        this.thresholdEntry = thresholdEntry;
    }

    private synchronized void analyzeEnvelope(short[] samples) {
        this.envelopeBuf = new float[samples.length];

        float attackGain = AudioUtil.gainCoefficient(sampleRate, 0.001F / 1000F);
        float releaseGain = AudioUtil.gainCoefficient(sampleRate, 60F / 1000F);

        float env = this.envelope;
        for (int i = 0; i < samples.length; i++) {
            float envIn = Math.abs(((float) samples[i]) / 0x8000);
            if (env < envIn) {
                env = envIn + attackGain * (env - envIn);
            } else {
                env = envIn + releaseGain * (env - envIn);
            }

            this.envelopeBuf[i] = Math.max(this.envelopeBuf[i], env);
        }
        this.envelope = envelopeBuf[samples.length - 1];
    }

    @Override
    public short[] process(short[] samples) {
        analyzeEnvelope(samples);
        limit(samples);
        return samples;
    }

    @Override
    public boolean isEnabled() {
        return activeEntry.value();
    }

    public synchronized void limit(short[] samples) {
        float limiterThreshold;

        if (thresholdEntry != null) {
            limiterThreshold = thresholdEntry.value();
        } else if (threshold != null) {
            limiterThreshold = threshold;
        } else {
            limiterThreshold = -6F;
        }


        for (int i = 0; i < samples.length; i++) {
            float envDB = AudioUtil.mulToDB(this.envelopeBuf[i]);

            float limiterGain = SLOPE * (limiterThreshold - envDB);
            limiterGain = AudioUtil.dbToMul(Math.min(0, limiterGain));

            samples[i] *= limiterGain * OUTPUT_GAIN;
        }
    }
}
