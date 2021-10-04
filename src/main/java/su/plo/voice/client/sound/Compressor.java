package su.plo.voice.client.sound;

import su.plo.voice.client.utils.AudioUtils;

public class Compressor {
    private float[] envelopeBuf = new float[0];
    private float envelope;

    public void compress(byte[] audio) {
        short[] samples = new short[audio.length / 2];
        float[] audioFloats = new float[samples.length];
        for (int i = 0; i < audio.length; i += 2) {
            samples[i / 2] = AudioUtils.bytesToShort(audio[i], audio[i + 1]);
            audioFloats[i / 2] = ((float)samples[i / 2]) / 0x8000;
        }

        analyzeEnvelope(audioFloats);
        process(audioFloats);

        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = (short) (audioFloats[i / 2] * 0x8000);

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);
        }
    }

    private void analyzeEnvelope(float[] samples) {
        if (envelopeBuf.length < samples.length) {
            float[] newBuf = new float[samples.length];
            System.arraycopy(this.envelopeBuf, 0, newBuf, 0, this.envelopeBuf.length);
            this.envelopeBuf = newBuf;
        }

        float attackGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), 6F / 1000F);
        float releaseGain = AudioUtils.gainCoefficient(Recorder.getSampleRate(), 60F / 1000F);

        float env = this.envelope;
        for (int i = 0; i < samples.length; i += 1) {
            float envIn = samples[i];
            if (env < envIn) {
                env = envIn + attackGain * (env - envIn);
            } else {
                env = envIn + releaseGain * (env - envIn);
            }

            this.envelopeBuf[i] = Math.max(this.envelopeBuf[i], env);
        }
        this.envelope = envelopeBuf[samples.length - 1];
    }

    private void process(float[] samples) {
        float limiterSlope = 1.0F;

        float compressorSlope = 1.0F - (1.0F / 10.0F);
        float outputGain = AudioUtils.dbToMul(0.0F);

        for (int i = 0; i < samples.length; i++) {
            float envDB = AudioUtils.mulToDB(this.envelopeBuf[i]);

            float compressorGain = compressorSlope * (-18.0f - envDB);
            compressorGain = AudioUtils.dbToMul(compressorGain);

//            samples[i] *= compressorGain * outputGain;


            float limiterGain = limiterSlope * (-6.0F - envDB);
            limiterGain = AudioUtils.dbToMul(limiterGain);

            samples[i] *= limiterGain * outputGain;
        }
    }
}
