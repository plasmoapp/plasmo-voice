package su.plo.voice.api.util;

/**
 * Utility class for audio-related operations.
 */
public final class AudioUtil {

    /**
     * Converts an array of bytes to an array of shorts.
     *
     * @param bytes The byte array to convert.
     * @return An array of shorts.
     */
    public static short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            shorts[i / 2] = bytesToShort(bytes[i], bytes[i + 1]);
        }

        return shorts;
    }

    /**
     * Converts an array of shorts to an array of bytes.
     *
     * @param shorts The short array to convert.
     * @return An array of bytes.
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
     * Converts two bytes to one short.
     *
     * @param b1 The first byte.
     * @param b2 The second byte.
     * @return A short value.
     */
    public static short bytesToShort(byte b1, byte b2) {
        return (short) (((b2 & 0xFF) << 8) | (b1 & 0xFF));
    }

    /**
     * Converts a short to an array of two bytes.
     *
     * @param s The short value to convert.
     * @return An array of two bytes.
     */
    public static byte[] shortToBytes(short s) {
        return new byte[]{(byte) (s & 0xFF), (byte) ((s >> 8) & 0xFF)};
    }

    /**
     * Converts an array of floats to an array of shorts.
     *
     * @param floats The float array to convert.
     * @return An array of shorts.
     */
    public static short[] floatsToShorts(float[] floats) {
        short[] shorts = new short[floats.length];

        for(int i = 0; i < floats.length; i++) {
            shorts[i] = Float.valueOf(
                    Math.min( // Clamp to prevent overdrive causing clipping (https://github.com/remjey/mumble/commit/f16b47c81aceaf0c8704b355d9316bf685cb3704)
                            Short.MAX_VALUE,
                            Math.max(
                                    Short.MIN_VALUE,
                                    floats[i]
                            )
                    )
            ).shortValue();
        }

        return shorts;
    }

    /**
     * Converts an array of shorts to an array of floats.
     *
     * @param input The short array to convert.
     * @return An array of floats.
     */
    public static float[] shortsToFloats(short[] input) {
        float[] ret = new float[input.length];

        for(int i = 0; i < input.length; i++) {
            ret[i] = Short.valueOf(input[i]).floatValue(); // 3Head
//            if ((input[i * 2 + 1] & 128) != 0) {
//                ret[i] = (float)(-32768 + ((input[i * 2 + 1] & 127) << 8) | input[i * 2] & 255);
//            } else {
//                ret[i] = (float)(input[i * 2 + 1] << 8 & '\uff00' | input[i * 2] & 255);
//            }
        }

        return ret;
    }

    /**
     * Checks if any sample audio level is greater than the specified minimum audio level.
     *
     * @param samples       The audio samples.
     * @param minAudioLevel The minimum audio level to check against.
     * @return {@code true} if any sample audio level is greater than or equal to the minimum audio level, {@code false} otherwise.
     */
    public static boolean containsMinAudioLevel(byte[] samples, double minAudioLevel) {
        for (int i = 0; i < samples.length; i += 100) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 100, samples.length));

            if (level >= minAudioLevel) return true;
        }

        return false;
    }

    /**
     * Checks if any sample audio level is greater than the specified minimum audio level.
     *
     * @param samples       The audio samples.
     * @param minAudioLevel The minimum audio level to check against.
     * @return {@code true} if any sample audio level is greater than or equal to the minimum audio level, {@code false} otherwise.
     */
    public static boolean containsMinAudioLevel(short[] samples, double minAudioLevel) {
        for (int i = 0; i < samples.length; i += 50) {
            double level = calculateAudioLevel(samples, i, Math.min(i + 50, samples.length));

            if (level >= minAudioLevel) return true;
        }

        return false;
    }

    /**
     * Calculates the audio level of a range of bytes.
     *
     * @param samples The audio samples.
     * @param offset  The offset from the start of the samples array.
     * @param length  The number of samples to process for the calculation.
     * @return The calculated audio level.
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
     * Calculates the audio level of a range of shorts.
     *
     * @param samples The audio samples.
     * @param offset  The offset from the start of the samples array.
     * @param length  The number of samples to process for the calculation.
     * @return The calculated audio level.
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
     * Converts RMS and sample count to an audio level.
     *
     * @param rms          Root Mean Square (RMS) value.
     * @param sampleCount  The count of samples.
     * @return The audio level.
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
     * Calculates the highest audio level in an array of shorts.
     *
     * @param samples The audio samples.
     * @return The highest audio level.
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
     * Converts an audio level to a double in the range [0, 1].
     *
     * @param audioLevel The audio level to convert.
     * @return The converted double value.
     */
    public static double audioLevelToDoubleRange(double audioLevel) {
        return 1D - (Math.max(-60D, audioLevel) / -60D);
    }


    /**
     * Converts a double in the range [0, 1] to an audio level.
     *
     * @param value The double value to convert.
     * @return The converted audio level.
     */
    public static double doubleRangeToAudioLevel(double value) {
        return Math.round((1D - value) * -60D);
    }

    /**
     * Converts stereo samples to mono bytes.
     *
     * @param samples The stereo samples to convert.
     * @return Mono audio data as an array of bytes.
     */
    public static byte[] convertToMonoBytes(short[] samples) {
        byte[] mono = new byte[samples.length];

        for (int i = 0; i < samples.length; i += 2) {
            byte[] monoSample = AudioUtil.shortToBytes((short) (samples[i] + samples[i + 1]));
            mono[i] = monoSample[0];
            mono[i + 1] = monoSample[1];
        }

        return mono;
    }

    /**
     * Converts stereo samples to mono shorts.
     *
     * @param samples The stereo samples to convert.
     * @return Mono audio data as an array of shorts.
     */
    public static short[] convertToMonoShorts(short[] samples) {
        short[] mono = new short[samples.length / 2];

        for (int i = 0; i < samples.length; i += 2) {
            mono[i / 2] = (short) ((samples[i] + samples[i + 1]) / 2);
        }

        return mono;
    }

    /**
     * Gets the highest absolute sample value in an array of shorts.
     *
     * @param samples The audio samples.
     * @return The highest absolute sample value.
     */
    public static short getHighestAbsoluteSample(short[] samples) {
        short max = 0;
        for (short sample : samples) {
            if (sample == Short.MIN_VALUE) {
                sample += 1;
            }

            short abs = (short) Math.abs(sample);
            if (abs > max) {
                max = abs;
            }
        }

        return max;
    }

    /**
     * Converts shorts to floats in the range [-1, 1].
     *
     * @param input The short array to convert.
     * @return The floats in the range [-1, 1].
     */
    public static float[] shortsToFloatsRange(short[] input) {
        float[] floats = new float[input.length];

        for(int i = 0; i < input.length; i++) {
            floats[i] = (float) input[i] / 0x8000;
        }

        return floats;
    }

    /**
     * Converts floats in the range [-1, 1] to shorts.
     *
     * @param input The float array to convert.
     * @return The shorts.
     */
    public static short[] floatsRangeToShort(float[] input) {
        short[] shorts = new short[input.length];

        for(int i = 0; i < input.length; i++) {
            shorts[i] = (short) (input[i] * 0x8000);
        }

        return shorts;
    }

    /**
     * Converts a multiplication factor to decibels (dB).
     *
     * @param mul The multiplication factor.
     * @return The corresponding decibel value.
     */
    public static float mulToDB(float mul) {
        return (mul == 0.0f) ? -Float.MAX_VALUE : (float) (20.0F * Math.log10(mul));
    }

    /**
     * Converts decibels (dB) to a multiplication factor.
     *
     * @param db The decibel value.
     * @return The corresponding multiplication factor.
     */
    public static float dbToMul(float db) {
        return Float.isFinite(db) ? (float) Math.pow(10.0F, db / 20.0F) : 0.0F;
    }

    /**
     * Converts decibels (dB) to a multiplication factor.
     *
     * @param db The decibel value.
     * @return The corresponding multiplication factor.
     */
    public static double dbToMul(double db) {
        return Double.isFinite(db) ? (float) Math.pow(10.0D, db / 20.0D) : 0.0D;
    }

    /**
     * Calculates the gain coefficient for a given sample rate and time.
     *
     * @param sampleRate The sample rate.
     * @param time       The time value.
     * @return The calculated gain coefficient.
     */
    public static float gainCoefficient(int sampleRate, float time) {
        return (float) Math.exp(-1.0f / (sampleRate * time));
    }

    private AudioUtil() {
    }
}
