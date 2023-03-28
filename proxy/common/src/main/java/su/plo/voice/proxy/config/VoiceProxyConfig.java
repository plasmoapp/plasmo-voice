package su.plo.voice.proxy.config;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.ConfigValidator;
import su.plo.config.entry.SerializableConfigEntry;
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

    private static final Logger LOGGER = LogManager.getLogger();

    private byte[] aesEncryptionKey = null;
    private UUID forwardingSecret = null;

    @ConfigField
    private String defaultLanguage = "en_us";

    @ConfigField
    private boolean debug = false;

    @ConfigField
    private VoiceHost host = new VoiceHost();

    @ConfigField(nullComment = "[servers]\n" +
            "farmworld = \"127.0.0.1:25565\"\n" +
            "overworld = \"127.0.0.1:25566\"")
    private Servers servers = new Servers();

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
