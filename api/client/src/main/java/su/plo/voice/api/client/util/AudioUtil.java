package su.plo.voice.api.client.util;

public final class AudioUtil {

    /**
     * Converts bytes to shorts
     *
     * @return short array
     */
    public static short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            shorts[i / 2] = bytesToShort(bytes[i], bytes[i + 1]);
        }

        return shorts;
    }

    /**
     * Converts shorts to bytes
     *
     * @return byte array
     */
    public static byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];

        for (int i = 0; i < bytes.length; i += 2) {
            byte[] sample = shortToBytes(shorts[i / 2]);
            bytes[i] = sample[0];
            bytes[i + 1] = sample[1];
        }

        return bytes;
    }

    /**
     * Converts two bytes to one short
     *
     * @return short
     */
    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    /**
     * Converts one short to two byte
     *
     * @return byte array
     */
    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    /**
     * Checks if any sample audio level greater than the min audio level
     *
     * @return return true if any sample audio level
     * greater than the min audio level
     */
    public static boolean containsMinAudioLevel(byte[] samples, double minAudioLevel) {
        for (int i = 0; i < samples.length; i += 100) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));

            if (level >= minAudioLevel) return true;
        }

        return false;
    }

    /**
     * Calculates the audio level
     *
     * @param samples the samples
     * @param offset offset from the start of the samples array
     * @param length count of samples to process the calculation
     *
     * @return the audio level
     */
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

    private AudioUtil() {
    }
}
