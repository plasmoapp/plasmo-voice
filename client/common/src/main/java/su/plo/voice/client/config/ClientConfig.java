package su.plo.voice.client.config;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.capture.Activation;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@Config
@Data
public final class ClientConfig {

    private static final TomlConfiguration toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @ConfigField
    private Voice voice = new Voice();

    @ConfigField
    private UI ui = new UI();

    @ConfigField
    private ConfigKeyBindings keyBindings = new ConfigKeyBindings();

    @ConfigField
    private Servers servers = new Servers();

    @Getter
    @Setter
    private File configFile;

    @Getter(AccessLevel.PRIVATE)
    @Setter
    private Executor asyncExecutor;

    public void save(boolean async) {
        if (configFile == null) throw new IllegalStateException("configFile is null");

        if (async) asyncExecutor.execute(this::save);
        else this.save();
    }

    private void save() {
        try {
            toml.save(ClientConfig.class, this, configFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save the config", e);
        }
    }

    @Data
    public static class Servers implements SerializableConfigEntry {

        private final Map<UUID, Server> servers = Maps.newConcurrentMap();

        public void put(@NotNull UUID serverId, Server server) {
            servers.put(serverId, server);
        }

        public Optional<Server> getById(@NotNull UUID serverId) {
            return Optional.ofNullable(servers.get(serverId));
        }

        @Override
        public void deserialize(Object object) {
            Map<String, Object> serialized = (Map<String, Object>) object;

            for (Map.Entry<String, Object> entry : serialized.entrySet()) {
                Server server = new Server();
                toml.deserialize(server, entry.getValue());

                put(UUID.fromString(entry.getKey()), server);
            }
        }

        @Override
        public Object serialize() {
            Map<String, Object> serialized = Maps.newHashMap();

            servers.forEach((serverId, server) ->
                    serialized.put(serverId.toString(), toml.serialize(server))
            );

            return serialized;
        }
    }

    @Data
    public static class Server implements SerializableConfigEntry {

        private ConfigClientActivation proximityActivation;
        private Map<UUID, ConfigClientActivation> activationById = Maps.newConcurrentMap();

        public void put(UUID activationId, ConfigClientActivation activation) {
            activationById.put(activationId, activation);
        }

        public Optional<ConfigClientActivation> getActivation(UUID id) {
            return Optional.ofNullable(activationById.get(id));
        }

        public ConfigClientActivation getActivation(UUID id, Activation serverActivation) {
            return activationById.computeIfAbsent(
                    id,
                    (activationId) -> createActivation(serverActivation)
            );
        }

        public ConfigClientActivation getProximityActivation(Activation serverActivation) {
            if (proximityActivation == null) {
                this.proximityActivation = createActivation(serverActivation);
            }

            return proximityActivation;
        }

        @Override
        public void deserialize(Object o) {
            Map<String, Object> serialized = (Map<String, Object>) o;

            serialized.forEach((id, serializedActivation) -> {
                if (id.equals("proximity")) {
                    proximityActivation = new ConfigClientActivation();
                    toml.deserialize(proximityActivation, serializedActivation);
                    return;
                }

                ConfigClientActivation activation = new ConfigClientActivation();
                toml.deserialize(activation, serializedActivation);
                put(UUID.fromString(id), activation);
            });
        }

        @Override
        public Object serialize() {
            Map<String, Object> serialized = Maps.newHashMap();

            if (!proximityActivation.isDefault()) {
                serialized.put("proximity", toml.serialize(proximityActivation));
            }
            for (Map.Entry<UUID, ConfigClientActivation> entry : activationById.entrySet()) {
                UUID activationId = entry.getKey();
                ConfigClientActivation activation = entry.getValue();

                if (!activation.isDefault()) {
                    serialized.put(activationId.toString(), toml.serialize(activation));
                }
            }

            return serialized;
        }

        private ConfigClientActivation createActivation(Activation serverActivation) {
            ConfigClientActivation activation = new ConfigClientActivation();
            activation.getConfigDistance().set(serverActivation.getDefaultDistance());
            activation.getConfigDistance().setDefault(
                    serverActivation.getDefaultDistance(),
                    serverActivation.getMinDistance(),
                    serverActivation.getMaxDistance()
            );

            return activation;
        }
    }

    @Config
    @Data
    public static class Voice {

        @ConfigField
        private ConfigEntry<Boolean> disabled = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> microphoneDisabled = new ConfigEntry<>(false);

        @ConfigField
        private DoubleConfigEntry activationThreshold = new DoubleConfigEntry(-30D, -60D, 0);

        @ConfigField
        private ConfigEntry<String> inputDevice = new ConfigEntry<>("");

        @ConfigField
        private ConfigEntry<String> outputDevice = new ConfigEntry<>("");

        @ConfigField
        private ConfigEntry<Boolean> useJavaxInput = new ConfigEntry<>(false);

        @ConfigField
        private DoubleConfigEntry microphoneVolume = new DoubleConfigEntry(1D, 0D, 2D);

        @ConfigField
        private ConfigEntry<Boolean> noiseSuppression = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> stereoCapture = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> stereoToMonoSources = new ConfigEntry<>(false);

        @ConfigField
        private CategoryVolumes volumes = new CategoryVolumes();

        @ConfigField
        private ConfigEntry<Boolean> listenerCameraRelative = new ConfigEntry<>(true);

        @ConfigField(path = "activation_type")
        protected EnumConfigEntry<ClientActivation.Type> activationType = new EnumConfigEntry<>(
                ClientActivation.Type.class,
                ClientActivation.Type.PUSH_TO_TALK
        );

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

            public synchronized DoubleConfigEntry getVolume(@NotNull String category) {
                return map.computeIfAbsent(
                        category,
                        (c) -> new DoubleConfigEntry(1D, 0D, 2D)
                );
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
                this.map.forEach((key, entry) -> {
                    if (!entry.isDefault()) serialized.put(key, entry.value());
                });
                return serialized;
            }
        }
    }

    @Config
    @Data
    public static class UI {

        @ConfigField(path = "activation_icon_position")
        protected EnumConfigEntry<ActivationIconPosition> activationIconPosition = new EnumConfigEntry<>(
                ActivationIconPosition.class,
                ActivationIconPosition.BOTTOM_CENTER
        );

        public enum ActivationIconPosition {
            TOP_LEFT(16, 16),
            TOP_CENTER(null, 16),
            TOP_RIGHT(-16, 16),
            BOTTOM_LEFT(16, -16),
            BOTTOM_CENTER(null, -38),
            BOTTOM_RIGHT(-16, -16);

            @Getter
            private final Integer x;
            @Getter
            private final Integer y;

            ActivationIconPosition(Integer x, Integer y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
