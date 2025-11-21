package su.plo.voice.proxy.config;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.ConfigValidator;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.slib.api.logging.McLogger;
import su.plo.slib.api.logging.McLoggerFactory;
import su.plo.voice.api.proxy.config.ProxyConfig;
import su.plo.voice.proxy.util.AddressUtil;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

@Config
@Data
@Accessors(fluent = true)
public final class VoiceProxyConfig implements ProxyConfig {

    private static final McLogger LOGGER = McLoggerFactory.createLogger("VoiceProxyConfig");

    private byte[] aesEncryptionKey = null;
    private UUID forwardingSecret = null;

    // default sample rate for API
    @Getter
    private final int sampleRate = 48_000;

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

    @ConfigField(comment = "MTU size used for Opus encoder creation via API")
    @ConfigValidator(
            value = MtuSizeValidator.class,
            allowed = "128-5000"
    )
    private int mtuSize = 1024;

    @ConfigField
    private VoiceHost host = new VoiceHost();

    @ConfigField(
            comment = "Override voice server addresses for specific backend servers\nBy default, uses the same address as the Minecraft backend server (from Velocity/BungeeCord config)",
            nullComment = "[servers]\nfarmworld = \"127.0.0.1:25565\"\noverworld = \"127.0.0.1:25566\""
    )
    private Servers servers = new Servers();

    @NoArgsConstructor
    public static class MtuSizeValidator implements Predicate<Object> {

        @Override
        public boolean test(Object o) {
            if (!(o instanceof Long)) return false;
            long mtuSize = (long) o;
            return mtuSize >= 128 && mtuSize <= 5000;
        }
    }

    @Data
    @Accessors(fluent = true)
    public static class Servers implements SerializableConfigEntry {

        private Map<String, String> serverByName = Maps.newConcurrentMap();

        public void put(@NotNull String name, @NotNull String address) {
            AddressUtil.parseAddress(address);
            serverByName.put(name, address);
        }

        public void remove(@NotNull String name) {
            serverByName.remove(name);
        }

        public Set<Map.Entry<String, String>> entrySet() {
            return serverByName.entrySet();
        }

        @Override
        public void deserialize(Object object) {
            if (object instanceof Map) {
                Map<String, String> serializedServers = (Map<String, String>) object;

                serializedServers.forEach((name, address) -> {
                    try {
                        put(name, address);
                    } catch (Exception e) {
                        LOGGER.error("Server {} has invalid address {}", name, address, e);
                    }
                });
            }
        }

        @Override
        public Object serialize() {
            Map<String, String> serializedServers = Maps.newTreeMap();
            serializedServers.putAll(serverByName);
            return serializedServers;
        }
    }

    @Config
    @Data
    @Accessors(fluent = true)
    public static class VoiceHost implements Host {

        @ConfigField(comment = "IP address for the voice server to bind to\n0.0.0.0 = bind to all available interfaces")
        private String ip = "0.0.0.0";

        @ConfigField(comment = "UDP port for the voice server\n0 = same port as Minecraft proxy server")
        @ConfigValidator(value = PortValidator.class, allowed = "0-65535")
        private int port = 0;

        @ConfigField(
                path = "public",
                comment = "Public IP and port that clients will connect to\nIf ip set to 0.0.0.0, ip from [host] will be used\nIf port set to 0, port from [host] will be used",
                nullComment = "[host.public]\nip = \"127.0.0.1\"\nport = 0"
        )
        private @Nullable Public hostPublic = null;

        @Config
        @Data
        @Accessors(fluent = true)
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
}
