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
import su.plo.slib.api.language.ServerLanguageFormat;
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

    @ConfigField
    private String defaultLanguage = "en_us";

    @ConfigField(
            comment = "Format used in languages\n" +
                    "LEGACY_AMPERSAND — default format using \"&\" for text styles\n\n" +
                    "MINI_MESSAGE — MiniMessage format, see https://docs.advntr.dev/minimessage/format.html.\n" +
                    "You need to use \"<argument:[index]>\" instead of \"%s\" or \"%[index]$s\" for language arguments.\n" +
                    "\"%s\" -> \"<argument:0>\"; \"%1$s\" -> \"<argument:0>\"; \"%2$s\" -> \"<argument:1>\""
    )
    private ServerLanguageFormat languageFormat = ServerLanguageFormat.LEGACY_AMPERSAND;

    @ConfigField
    private boolean debug = false;

    @ConfigField
    private boolean useCrowdinTranslations = true;

    @ConfigField
    private boolean checkForUpdates = true;

    @ConfigField(comment = "The MTU size on the proxy only needs to create Opus encoders using the API")
    @ConfigValidator(
            value = MtuSizeValidator.class,
            allowed = "128-5000"
    )
    private int mtuSize = 1024;

    @ConfigField
    private VoiceHost host = new VoiceHost();

    @ConfigField(nullComment = "[servers]\n" +
            "farmworld = \"127.0.0.1:25565\"\n" +
            "overworld = \"127.0.0.1:25566\"")
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
            Map<String, String> serializedServers = Maps.newHashMap();
            serializedServers.putAll(serverByName);
            return serializedServers;
        }
    }

    @Config
    @Data
    @Accessors(fluent = true)
    public static class VoiceHost implements Host {

        @ConfigField
        private String ip = "0.0.0.0";

        @ConfigField
        @ConfigValidator(value = PortValidator.class, allowed = "0-65535")
        private int port = 0;

        @ConfigField(path = "public")
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
