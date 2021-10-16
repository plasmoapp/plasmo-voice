package su.plo.voice.client.sound;

import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.utils.AudioUtils;

// todo priority sidechain
public class Compressor {
    private static final float compressorSlope = 1.0F - (1.0F / 10.0F);
    private static final float outputGain = AudioUtils.dbToMul(0.0F);
    private static final float attackTime = 6F;
    private static final float releaseTime = 60F;

    private final Limiter limiter = new Limiter();

    private float[] envelopeBuf = new float[0];
    private float envelope;

    public synchronized byte[] compress(byte[] audio) {
        float[] audioFloats = AudioUtils.bytesToFloats(audio);

        analyzeEnvelope(audioFloats);
        process(audioFloats);

        limiter.limit(audioFloats);

        return AudioUtils.floatsToBytes(audioFloats);
    }

    private synchronized void analyzeEnvelope(float[] samples) {
        this.envelopeBuf = new float[samples.length];

        float attackGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), attackTime / 1000F);
        float releaseGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), releaseTime / 1000F);

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
        float compressorThreshold = VoiceClient.getClientConfig().compressorThreshold.get();
//        float limiterThreshold = VoiceClient.getClientConfig().limiterThreshold.get();

        for (int i = 0; i < samples.length; i++) {
            float envDB = AudioUtils.mulToDB(this.envelopeBuf[i]);

            float compressorGain = compressorSlope * (compressorThreshold - envDB);
            compressorGain = AudioUtils.dbToMul(Math.min(0, compressorGain));

//            VoiceSettingsScreen.roflanDebugText = String.valueOf(compressorGain);

            samples[i] *= compressorGain * outputGain;

//            float limiterGain = limiterSlope * (limiterThreshold - envDB);
//            limiterGain = AudioUtils.dbToMul(Math.min(0, limiterGain));
//
//            samples[i] *= limiterGain * outputGain;
        }
    }
}
