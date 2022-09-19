package su.plo.voice.util;

import java.util.Random;

public final class RandomUtil {

    private static final Random random = new Random();

    public static float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    private RandomUtil() {
    }
}
