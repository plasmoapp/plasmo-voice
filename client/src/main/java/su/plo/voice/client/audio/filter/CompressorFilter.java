package su.plo.voice.client.audio.filter;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.AudioUtil;

@RequiredArgsConstructor
public final class CompressorFilter implements AudioFilter {

    private static final float SLOPE = 1F - (1F / 10F);
    private static final float OUTPUT_GAIN = AudioUtil.dbToMul(0F);
    private static final float ATTACK_TIME = 6F;
    private static final float RELEASE_TIME = 60F;

    private final int sampleRate;
    private final ConfigEntry<Boolean> activeEntry;
    private final IntConfigEntry thresholdEntry;

    private float[] envelopeBuf = new float[0];
    private float envelope;

    private synchronized void analyzeEnvelope(short[] samples) {
        this.envelopeBuf = new float[samples.length];

        float attackGain = AudioUtil.gainCoefficient(sampleRate, ATTACK_TIME / 1000F);
        float releaseGain = AudioUtil.gainCoefficient(sampleRate, RELEASE_TIME / 1000F);

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
    public @NotNull String getName() {
        return "compressor";
    }

    @Override
    public short[] process(short[] samples) {
        analyzeEnvelope(samples);
        compress(samples);
        return samples;
    }

    @Override
    public boolean isEnabled() {
        return activeEntry.value();
    }

    public synchronized short[] compress(short[] samples) {
        float compressorThreshold = thresholdEntry.value();

        for (int i = 0; i < samples.length; i++) {
            float envDB = AudioUtil.mulToDB(envelopeBuf[i]);

            float compressorGain = SLOPE * (compressorThreshold - envDB);
            compressorGain = AudioUtil.dbToMul(Math.min(0, compressorGain));

            samples[i] *= compressorGain * OUTPUT_GAIN;
        }

        return samples;
    }
}
