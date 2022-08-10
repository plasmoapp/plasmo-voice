package su.plo.voice.client.config;

import com.google.common.collect.Maps;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Config
@Data
public final class ClientConfig {

    private static final TomlConfiguration toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @ConfigField
    private Voice voice = new Voice();

    @ConfigField
    private ConfigKeyBindings keyBindings = new ConfigKeyBindings();

    @ConfigField
    private Servers servers = new Servers();

    @Data
    public static class Servers implements SerializableConfigEntry {

        private final Map<UUID, Server> servers = Maps.newHashMap();

        public synchronized void put(@NotNull UUID serverId, Server server) {
            servers.put(serverId, server);
        }

        public synchronized Optional<Server> getById(@NotNull UUID serverId) {
            return Optional.ofNullable(servers.get(serverId));
        }

        @Override
        public synchronized void deserialize(Object object) {
            Map<String, Object> serialized = (Map<String, Object>) object;

            for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                Server server = new Server();
                toml.deserialize(server, entry.getValue());

                put(UUID.fromString(entry.getKey()), server);
            }
        }

        @Override
        public synchronized Object serialize() {
            Map<String, Object> serialized = Maps.newHashMap();
            servers.forEach((serverId, server) ->
                    serialized.put(serverId.toString(), toml.serialize(server))
            );

            return serialized;
        }
    }

    @Config
    @Data
    public static class Server {

        @ConfigField
        private IntConfigEntry distance = new IntConfigEntry(0, 0, Short.MAX_VALUE);

        @ConfigField
        private IntConfigEntry priorityDistance = new IntConfigEntry(0, 0, Short.MAX_VALUE);
    }

    @Config
    @Data
    public static class Voice {

        @ConfigField
        private ConfigEntry<Boolean> disabled = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> microphoneDisabled = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> voiceActivation = new ConfigEntry<>(false);

        @ConfigField
        private DoubleConfigEntry voiceActivationThreshold = new DoubleConfigEntry(-30D, -60D, 0);

        @ConfigField
        private ConfigEntry<String> inputDevice = new ConfigEntry<>("");

        @ConfigField
        private ConfigEntry<String> outputDevice = new ConfigEntry<>("");

        @ConfigField
        private ConfigEntry<Boolean> useJavaxInput = new ConfigEntry<>(false);

        @ConfigField
        private CategoryVolumes volumes = new CategoryVolumes();

        @Data
        public static class CategoryVolumes implements SerializableConfigEntry {

            private Map<String, DoubleConfigEntry> map = Maps.newHashMap();

            public CategoryVolumes() {
                setVolume("general", 1D);
                setVolume("priority", 1D);
            }

            public synchronized void setVolume(@NotNull String category, double volume) {
                map.put(category, new DoubleConfigEntry(volume, 0D, 2D));
            }

            public synchronized Optional<DoubleConfigEntry> getVolume(@NotNull String category) {
                return Optional.ofNullable(map.get(category));
            }

            @Override
            public synchronized void deserialize(Object object) {
                try {
                    Map<String, Object> map = (Map<String, Object>) object;
                    map.forEach((key, value) -> setVolume(key, (double) value));
                } catch (ClassCastException ignored) {
                }
            }

            @Override
            public synchronized Object serialize() {
                Map<String, Double> serialized = Maps.newHashMap();
                this.map.forEach((key, entry) -> serialized.put(key, entry.value()));
                return serialized;
            }
        }
    }
}
