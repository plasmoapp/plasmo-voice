package su.plo.voice.server.config;

import com.electronwill.nightconfig.core.conversion.*;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Data
public final class ServerConfig {

    @Path("host")
    Host host;

    @Path("voice")
    Voice voice;

    @Data
    public static class Host {

        @Path("ip")
        String ip;

        @Path("port")
        @SpecIntInRange(min = 0, max = 65535)
        int port;

        @Path("public")
        @Nullable Public hostPublic;

        @Data
        public static class Public {
            @Path("ip")
            String ip;

            @Path("port")
            @SpecIntInRange(min = 0, max = 65535)
            int port;
        }
    }

    @Data
    public static class Voice {

        @Path("sample_rate")
        @SpecValidator(SampleRateValidator.class)
        int sampleRate;

        @Path("distances")
        @Conversion(DistancesSorter.class)
        List<Integer> distances;

        @Path("default_distance")
        int defaultDistance;

        @Path("max_priority_distance")
        int maxPriorityDistance;

        @Path("fade_divisor")
        int fadeDivisor;

        @Path("priority_fade_divisor")
        int priorityFadeDivisor;

        @Path("client_mod_required")
        boolean clientModRequired;

        static class DistancesSorter implements Converter<List<Integer>, List<Integer>> {

            @Override
            public List<Integer> convertToField(List<Integer> value) {
                Collections.sort(value);
                return value;
            }

            @Override
            public List<Integer> convertFromField(List<Integer> value) {
                Collections.sort(value);
                return value;
            }
        }

        static class SampleRateValidator implements Predicate<Object> {

            @Override
            public boolean test(Object o) {
                if (!(o instanceof Integer)) return false;
                int sampleRate = (int) o;
                return sampleRate == 8_000
                        || sampleRate == 12_000
                        || sampleRate == 24_000
                        || sampleRate == 48_000;
            }
        }
    }
}
