package su.plo.voice.server.config;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.ConfigFieldProcessor;
import su.plo.config.ConfigValidator;
import su.plo.voice.api.server.config.ServerConfig;
import su.plo.voice.proto.data.config.PlayerIconVisibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Config
@Data
@Accessors(fluent = true)
public final class VoiceServerConfig implements ServerConfig {

    @ConfigField(comment = "Used to store server-related config file on the client\nSet it to a single value on different servers if you want them to share config")
    private String serverId = UUID.randomUUID().toString();

    @ConfigField(
            comment =
                    "Language used when client's language doesn't exist\n\n" +
                    "By default, client's language used for translations\n" +
                    "For example, if default_language is set to ja_jp but the client uses en_us, then en_us will be used\n" +
                    "If you want to use one specific language for all clients, uncomment and set forced_language below"
    )
    private String defaultLanguage = "en_us";

    @ConfigField(
            comment = "Language used for all clients",
            nullComment = "forced_language = \"ja_jp\""
    )
    private @Nullable String forcedLanguage = null;

    @ConfigField(comment = "Enables debug logging")
    private boolean debug = false;

    @ConfigField(comment = "Enables translations from Crowdin")
    private boolean useCrowdinTranslations = true;

    @ConfigField
    private boolean checkForUpdates = true;

    @ConfigField
    private Host host = new Host();

    @ConfigField
    private Voice voice = new Voice();

    @Config
    @Data
    @Accessors(fluent = true)
    @EqualsAndHashCode
    public static class Host implements ServerConfig.Host {

        private UUID forwardingSecret = null;

        @ConfigField(comment = "IP address for the voice server to bind to\n0.0.0.0 = bind to all available interfaces")
        private String ip = "0.0.0.0";

        @ConfigField(comment = "UDP port for the voice server\n0 = same port as Minecraft server")
        @ConfigValidator(value = PortValidator.class, allowed = "0-65535")
        private int port = 0;

        @ConfigField(
                path = "public",
                comment = "Public IP and port that clients will connect to\nIf ip set to 0.0.0.0, ip from [host] will be used\nIf port set to 0, port from [host] will be used\nIgnored when using BungeeCord/Velocity (proxy manages public address instead)",
                nullComment = "[host.public]\nip = \"127.0.0.1\"\nport = 0"
        )
        private @Nullable Public hostPublic = null;

        @Config
        @Data
        @Accessors(fluent = true)
        @EqualsAndHashCode
        public static class Public implements ServerConfig.Host.Public {

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
    @Accessors(fluent = true)
    public static class Voice implements ServerConfig.Voice {

        private byte[] aesEncryptionKey = null;

        @ConfigField(comment = "The maximum amount, in blocks, to broadcast audio packets past the audible proximity distance.\nThe plugin will send audio packets to players within 2x the proximity distance, or the distance plus this number - whichever is smaller.")
        private int maxExtraAudioBroadcastDistance = 16;

        @ConfigField(comment = "Supported sample rates: [8000, 12000, 24000, 48000]\nDon't change this to reduce bandwidth; adjust opus bitrate instead")
        @ConfigValidator(
                value = SampleRateValidator.class,
                allowed = {"8000", "16000", "24000", "48000"}
        )
        private int sampleRate = 48_000;

        @ConfigField(comment = "Time before voice client connection is considered timed out\nServer will try to automatically reconnect the client after timeout")
        @ConfigValidator(
                value = KeepAliveTimeoutValidator.class,
                allowed = "1000-120000"
        )
        private int keepAliveTimeoutMs = 15_000;

        @ConfigField(comment = "Maximum packet size for voice data transmission")
        @ConfigValidator(
                value = MtuSizeValidator.class,
                allowed = "128-5000"
        )
        private int mtuSize = 1024;

        @ConfigField(comment = "Requires players to have the Plasmo Voice mod installed")
        private boolean clientModRequired = false;

        @ConfigField(comment = "Time before player is kicked if the mod is required but not installed")
        private long clientModRequiredCheckTimeoutMs = 3_000L;

        @ConfigField(comment = "Minimum required version for a client with the mod to connect to the voice server\nThis will not kick the player but will simply not connect them to the voice server and will suggest downloading the required version")
        private @NotNull String clientModMinVersion = "2.0.0";

        @ConfigField
        private Proximity proximity = new Proximity();

        @ConfigField
        private Opus opus = new Opus();

        @ConfigField
        private PlayerIcon playerIcon = new PlayerIcon();

        @ConfigField(
                comment = "Set custom weights for specific activations and source lines\nWeight determines display order in client menu and overlay (lower = higher position)"
        )
        private Weights weights = new Weights();

        @Config
        @Data
        @Accessors(fluent = true)
        public static class PlayerIcon implements ServerConfig.Voice.PlayerIcon {
            @ConfigField(comment = "Controls which icons are hidden above player heads\nLeave empty to show all icons (default)\nTo hide all icons, use: [\"HIDE_NOT_INSTALLED\", \"HIDE_VOICE_CHAT_DISABLED\", \"HIDE_SERVER_MUTED\", \"HIDE_CLIENT_MUTED\", \"HIDE_SOURCE_ICON\"]\n\nAvailable options:\nHIDE_NOT_INSTALLED - hides icon when player doesn't have Plasmo Voice installed\nHIDE_VOICE_CHAT_DISABLED - hides icon when player disables voice chat on the client\nHIDE_SERVER_MUTED - hides icon when player's voice chat is muted on the server\nHIDE_CLIENT_MUTED - hides icon when player is muted on the client using Volume tab\nHIDE_SOURCE_ICON - hides icon when player is talking")
            private @NotNull List<PlayerIconVisibility> visibility = new ArrayList<>(PlayerIconVisibility.none());

            @ConfigField(comment = "Controls the y offset of the icon above player heads")
            private double yOffset = 0.0;
        }

        @Config
        @Data
        @Accessors(fluent = true)
        public static class Weights implements ServerConfig.Voice.Weights {

            @ConfigField(
                    path = "activations",
                    nullComment = "[voice.weights.activations]\ngroups = 0"
            )
            private Map<String, Integer> weightByActivationName = Maps.newTreeMap();
            @ConfigField(
                    path = "source_lines",
                    nullComment = "[voice.weights.source_lines]\ngroups = 0"
            )
            private Map<String, Integer> weightBySourceLineName = Maps.newTreeMap();

            @Override
            public Optional<Integer> getActivationWeight(@NotNull String activationName) {
                return Optional.ofNullable(weightByActivationName.get(activationName));
            }

            @Override
            public Optional<Integer> getSourceLineWeight(@NotNull String sourceLineName) {
                return Optional.ofNullable(weightBySourceLineName.get(sourceLineName));
            }
        }

        @Config
        @Data
        @Accessors(fluent = true)
        public static class Proximity implements ServerConfig.Voice.Proximity {

            @ConfigField
            @ConfigFieldProcessor(DistancesSorter.class)
            private List<Integer> distances = Arrays.asList(8, 16, 32);

            @ConfigField
            private int defaultDistance = 16;

            @NoArgsConstructor
            public static class DistancesSorter implements Function<List<Long>, List<Integer>> {

                @Override
                public List<Integer> apply(List<Long> distances) {
                    Collections.sort(distances);
                    return distances.stream().map(Long::intValue).collect(Collectors.toList());
                }
            }
        }

        @Config
        @Data
        @Accessors(fluent = true)
        public static class Opus implements ServerConfig.Voice.Opus {

            @ConfigField(comment = "Opus application mode\nSupported values: VOIP, AUDIO, RESTRICTED_LOWDELAY\nDefault is VOIP")
            @ConfigValidator(
                    value = ModeValidator.class,
                    allowed = {"VOIP", "AUDIO", "RESTRICTED_LOWDELAY"}
            )
            private String mode = "VOIP";

            @ConfigField(comment = "Opus bitrate\nSupported values: -1000 (auto), -1 (max), [500-512_000]\nDefault is -1000")
            @ConfigValidator(
                    value = BitrateValidator.class,
                    allowed = {"-1000 (auto)", "-1 (max)", "500-512000"}
            )
            private int bitrate = -1000;

            @NoArgsConstructor
            public static class ModeValidator implements Predicate<Object> {

                @Override
                public boolean test(Object o) {
                    if (!(o instanceof String)) return false;
                    String mode = (String) o;

                    return mode.equals("VOIP") || mode.equals("AUDIO") || mode.equals("RESTRICTED_LOWDELAY");
                }
            }

            @NoArgsConstructor
            public static class BitrateValidator implements Predicate<Object> {

                @Override
                public boolean test(Object o) {
                    if (!(o instanceof Long)) return false;
                    long bitrate = (Long) o;

                    return bitrate == -1 || bitrate == -1000 || (bitrate >= 500 && bitrate <= 512_000);
                }
            }
        }

        @NoArgsConstructor
        public static class MtuSizeValidator implements Predicate<Object> {

            @Override
            public boolean test(Object o) {
                if (!(o instanceof Long)) return false;
                long mtuSize = (long) o;
                return mtuSize >= 128 && mtuSize <= 5000;
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

        @NoArgsConstructor
        public static class KeepAliveTimeoutValidator implements Predicate<Object> {

            @Override
            public boolean test(Object o) {
                if (o == null) return true;

                if (!(o instanceof Long)) return false;
                long keepAliveTimeout = (Long) o;
                return keepAliveTimeout >= 1_000 && keepAliveTimeout <= 120_000;
            }
        }
    }
}
