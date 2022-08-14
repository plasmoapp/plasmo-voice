package su.plo.voice.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestVersionUtil {

    @Test
    public void testVersion() {
        System.out.println(Arrays.toString(VersionUtil.parseVersion("1.0.0")));
        System.out.println(Arrays.toString(VersionUtil.parseVersion("1.0.0-SNAPSHOT")));
        try {
            System.out.println(Arrays.toString(VersionUtil.parseVersion("Pepega")));
        } catch (IllegalArgumentException ignored) {
            // ok
        }
    }
}
