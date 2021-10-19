package su.plo.voice.client.utils;

public class AudioUtils {
    public static byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];

        for (int i = 0; i < bytes.length; i += 2) {
            byte[] sample = shortToBytes(shorts[i / 2]);
            bytes[i] = sample[0];
            bytes[i + 1] = sample[1];
        }

        return bytes;
    }

    public static float[] bytesToFloats(byte[] bytes) {
        float[] floats = new float[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            floats[i / 2] = ((float) AudioUtils.bytesToShort(bytes[i], bytes[i + 1])) / 0x8000;
        }

        return floats;
    }

    public static byte[] floatsToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 2];
        for (int i = 0; i < bytes.length; i += 2) {
            short audioSample = (short) (floats[i / 2] * 0x8000);

            bytes[i] = (byte) audioSample;
            bytes[i + 1] = (byte) (audioSample >> 8);
        }

        return bytes;
    }

    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    public static float mulToDB(float mul) {
        return (mul == 0.0f) ? -Float.MAX_VALUE : (float) (20.0F * Math.log10(mul));
    }

    public static float dbToMul(float db) {
        return Float.isFinite(db) ? (float) Math.pow(10.0F, db / 20.0F) : 0.0F;
    }

    public static float gainCoefficient(int sampleRate, float time) {
        return (float)Math.exp(-1.0f / (sampleRate * time));
    }

    public static int getActivationOffset(byte[] samples, double activationLevel) {
        int highestPos = -1;
        for (int i = 0; i < samples.length; i += 100) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level >= activationLevel) {
                highestPos = i;
            }
        }
        return highestPos;
    }

    public static double getHighestAudioLevel(byte[] samples) {
        double highest = -127D;
        for (int i = 0; i < samples.length; i += 100) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));
            if (level > highest) {
                highest = level;
            }
        }
        return highest;
    }

    public static double calculateAudioLevel(byte[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = offset; i < length; i += 2) {
            double sample = (double) bytesToShort(samples[i], samples[i + 1]) / Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = length / 2;

        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    public static void adjustVolume(byte[] audio, float volume) {
        for (int i = 0; i < audio.length; i += 2) {
            short audioSample = bytesToShort(audio[i], audio[i + 1]);

            audioSample = (short) (audioSample * volume);

            audio[i] = (byte) audioSample;
            audio[i + 1] = (byte) (audioSample >> 8);
        }
    }
}
