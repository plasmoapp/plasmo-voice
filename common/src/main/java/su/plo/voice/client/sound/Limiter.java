package su.plo.voice.client.sound;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.utils.AudioUtils;

public class Limiter {
    private static final float limiterSlope = 1.0F;
    private static final float outputGain = AudioUtils.dbToMul(0.0F);

    private final Float threshold;
    private float[] envelopeBuf = new float[0];
    private float envelope;

    public Limiter(float threshold) {
        this.threshold = threshold;
    }

    public Limiter() {
        this.threshold = null;
    }

    public synchronized void limit(float[] audioFloats) {
        analyzeEnvelope(audioFloats);
        process(audioFloats);
    }

    private synchronized void analyzeEnvelope(float[] samples) {
        this.envelopeBuf = new float[samples.length];

        float attackGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), 0.001F / 1000F);
        float releaseGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), 60F / 1000F);

        float env = this.envelope;
        for (int i = 0; i < samples.length; i++) {
            float envIn = Math.abs(samples[i]);
            if (env < envIn) {
                env = envIn + attackGain * (env - envIn);
            } else {
                env = envIn + releaseGain * (env - envIn);
            }

            this.envelopeBuf[i] = Math.max(this.envelopeBuf[i], env);
        }
        this.envelope = envelopeBuf[samples.length - 1];
    }

    private synchronized void process(float[] samples) {
        float limiterThreshold;
        if (this.threshold != null) {
            limiterThreshold = this.threshold;
        } else {
            limiterThreshold = VoiceClient.getClientConfig().limiterThreshold.get();
        }

        for (int i = 0; i < samples.length; i++) {
            float envDB = AudioUtils.mulToDB(this.envelopeBuf[i]);

            float limiterGain = limiterSlope * (limiterThreshold - envDB);
            limiterGain = AudioUtils.dbToMul(Math.min(0, limiterGain));

            samples[i] *= limiterGain * outputGain;
        }
    }
}
