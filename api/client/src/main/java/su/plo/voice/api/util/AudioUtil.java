package su.plo.voice.api.util;

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

    private AudioUtil() {
    }
}
