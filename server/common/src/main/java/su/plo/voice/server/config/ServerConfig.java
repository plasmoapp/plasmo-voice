package su.plo.voice.server.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.ConfigFieldProcessor;
import su.plo.config.ConfigValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Config
@Data
public final class ServerConfig {

    @ConfigField(path = "server_id")
    private String serverId = UUID.randomUUID().toString();

    @ConfigField
    private Host host = new Host();

    @ConfigField
    private Voice voice = new Voice();

    @Config
    @Data
    public static class Host {

        @ConfigField
        private String ip = "0.0.0.0";

        @ConfigField
        @ConfigValidator(value = PortValidator.class, allowed = "0-65535")
        private int port = 0;

        @ConfigField(path = "public")
        private @Nullable Public hostPublic = null;

        @Config
        @Data
        public static class Public {
            @ConfigField
            private String ip = "127.0.0.1";

            @ConfigField
            @ConfigValidator(value = PortValidator.class, allowed = "0-65535")
            private int port = 0;
        }

        @NoArgsConstructor
        public static class PortValidator implements Predicate<Object> {

            @Override
            public boolean test(Object o) {
                if (!(o instanceof Long)) return false;
                long port = (long) o;
                return port >= 0 && port <= 65535;
            }
        }
    }

    @Config
    @Data
    public static class Voice {

        @ConfigField(path = "sample_rate")
        @ConfigValidator(
                value = SampleRateValidator.class,
                allowed = {"8000", "16000", "24000", "48000"}
        )
        private int sampleRate = 48_000;

        @ConfigField
        @ConfigFieldProcessor(DistancesSorter.class)
        private List<Integer> distances = Arrays.asList(8, 16, 32);

        @ConfigField(path = "default_distance")
        private int defaultDistance = 16;

        @ConfigField(path = "client_mod_required")
        private boolean clientModRequired = false;

        @NoArgsConstructor
        public static class DistancesSorter implements Function<List<Long>, List<Integer>> {

            @Override
            public List<Integer> apply(List<Long> distances) {
                Collections.sort(distances);
                return distances.stream().map(Long::intValue).collect(Collectors.toList());
            }
        }

        @NoArgsConstructor
        public static class SampleRateValidator implements Predicate<Object> {

            @Override
            public boolean test(Object o) {
                if (!(o instanceof Long)) return false;
                long sampleRate = (long) o;
                return sampleRate == 8_000
                        || sampleRate == 12_000
                        || sampleRate == 24_000
                        || sampleRate == 48_000;
            }
        }
    }
}
