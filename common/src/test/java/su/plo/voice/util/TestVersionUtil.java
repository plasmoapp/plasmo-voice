package su.plo.voice.util;

import org.junit.jupiter.api.Test;
import su.plo.voice.util.version.SemanticVersion;

public class TestVersionUtil {

    @Test
    public void testVersion() {
        System.out.println(SemanticVersion.parse("1.0.0"));
        System.out.println(SemanticVersion.parse("1.0.0-SNAPSHOT"));
        try {
            System.out.println(SemanticVersion.parse("Pepega"));
        } catch (IllegalArgumentException ignored) {
            // ok
        }
    }
}
