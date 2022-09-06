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
     * Checks if any sample audio level greater than the min audio level
     *
     * @return return true if any sample audio level
     * greater than the min audio level
     */
    public static boolean containsMinAudioLevel(short[] samples, double minAudioLevel) {
        for (int i = 0; i < samples.length; i += 50) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 50, samples.length));

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

        return calculateAudioLevelFromRMS(rms, samples.length / 2);
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
    public static double calculateAudioLevel(short[] samples, int offset, int length) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = offset; i < length; i++) {
            double sample = (double) samples[i] / Short.MAX_VALUE;
            rms += sample * sample;
        }

        return calculateAudioLevelFromRMS(rms, samples.length);
    }

    /**
     * Calculates the audio level from RMS and samples count
     *
     * @param rms root mean square
     * @param sampleCount count of samples
     *
     * @return the audio level
     */
    public static double calculateAudioLevelFromRMS(double rms, int sampleCount) {
        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;

        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    /**
     * Gets the highest audio level in the samples
     *
     * @param samples the samples
     *
     * @return the highest audio level
     */
    public static double calculateHighestAudioLevel(short[] samples) {
        double highest = -127D;

        for (int i = 0; i < samples.length; i += 50) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 50, samples.length));
            if (level > highest) highest = level;
        }

        return highest;
    }

    /**
     * Converts the audio level to double in range [0, 1]
     *
     * @return the double in range [0, 1]
     */
    public static double audioLevelToDoubleRange(double audioLevel) {
        return 1D - (Math.max(-60D, audioLevel) / -60D);
    }

    /**
     * Converts the double in range [0, 1] to audio level
     *
     * @return the audio level
     */
    public static double doubleRangeToAudioLevel(double value) {
        return Math.round((1D - value) * -60D);
    }

    private AudioUtil() {
    }
}
