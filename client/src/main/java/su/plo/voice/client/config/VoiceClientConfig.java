package su.plo.voice.client.config;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.*;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.api.client.config.ClientConfig;
import su.plo.voice.api.client.config.IconPosition;
import su.plo.voice.api.client.config.overlay.OverlayPosition;
import su.plo.voice.api.client.config.overlay.OverlaySourceState;
import su.plo.voice.api.client.config.overlay.OverlayStyle;
import su.plo.voice.client.config.capture.ConfigClientActivation;
import su.plo.voice.client.config.hotkey.ConfigHotkeys;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.line.SourceLine;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

// todo: need to rewrite this class, it's a mess
@Config
@Data
public final class VoiceClientConfig implements ClientConfig {

    private static final TomlConfiguration toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @ConfigField
    private BooleanConfigEntry debug = new BooleanConfigEntry(false);

    @ConfigField
    private BooleanConfigEntry disableCrowdin = new BooleanConfigEntry(false);

    @ConfigField
    private BooleanConfigEntry checkForUpdates = new BooleanConfigEntry(true);

    @ConfigField
    private Voice voice = new Voice();

    @ConfigField
    private Advanced advanced = new Advanced();

    @ConfigField
    private Activations activations = new Activations();


    @ConfigField
    private Overlay overlay = new Overlay();

    @ConfigField
    private ConfigHotkeys keyBindings = new ConfigHotkeys();

    @ConfigField
    private Servers servers = new Servers();

    @ConfigField
    private Addons addons = new Addons();

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

    private synchronized void save() {
        try {
            toml.save(VoiceClientConfig.class, this, configFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save the config", e);
        }
    }

    @Data
    public static class Servers implements SerializableConfigEntry {

        private final Map<UUID, Server> serverById = Maps.newConcurrentMap();

        public void put(@NotNull UUID serverId, Server server) {
            serverById.put(serverId, server);
        }

        public Optional<Server> getById(@NotNull UUID serverId) {
            return Optional.ofNullable(serverById.get(serverId));
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

            serverById.forEach((serverId, server) ->
                    serialized.put(serverId.toString(), toml.serialize(server))
            );

            return serialized;
        }
    }

    @Data
    public static class Activations implements SerializableConfigEntry {

        @Getter(AccessLevel.PRIVATE)
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
                    (activationId) -> new ConfigClientActivation()
            );
        }

        @Override
        public void deserialize(Object o) {
            Map<String, Object> serialized = (Map<String, Object>) o;

            serialized.forEach((id, serializedActivation) -> {
                ConfigClientActivation activation = new ConfigClientActivation();
                toml.deserialize(activation, serializedActivation);
                put(UUID.fromString(id), activation);
            });
        }

        @Override
        public Object serialize() {
            Map<String, Object> serialized = Maps.newHashMap();

            for (Map.Entry<UUID, ConfigClientActivation> entry : activationById.entrySet()) {
                UUID activationId = entry.getKey();
                ConfigClientActivation activation = entry.getValue();

                if (!activation.isDefault()) {
                    serialized.put(activationId.toString(), toml.serialize(activation));
                }
            }

            return serialized;
        }
    }

    @Data
    public static class Server implements SerializableConfigEntry {

        private Map<UUID, IntConfigEntry> activationDistances = Maps.newConcurrentMap();

        public void put(UUID activationId, IntConfigEntry distance) {
            activationDistances.put(activationId, distance);
        }

        public Optional<IntConfigEntry> getActivationDistance(UUID id) {
            return Optional.ofNullable(activationDistances.get(id));
        }

        public IntConfigEntry getActivationDistance(UUID id, Activation serverActivation) {
            return activationDistances.computeIfAbsent(
                    id,
                    (activationId) -> createActivationDistance(serverActivation)
            );
        }

        @Override
        public void deserialize(Object o) {
            Map<String, Object> serialized = (Map<String, Object>) o;
            if (serialized.containsKey("distances")) {
                Map<String, Object> distances = (Map<String, Object>) serialized.get("distances");

                distances.forEach((activationId, distance) -> {
                    IntConfigEntry entry = new IntConfigEntry(0, 0, Short.MAX_VALUE);
                    entry.set(((Long) distance).intValue());

                    put(UUID.fromString(activationId), entry);
                });
            }
        }

        @Override
        public Object serialize() {
            Map<String, Map<String, Object>> serialized = Maps.newHashMap();
            Map<String, Object> distances = Maps.newHashMap();

            activationDistances.forEach((activationId, entry) -> {
                if (entry.isDefault()) return;
                distances.put(activationId.toString(), entry.value());
            });

            if (distances.size() > 0) serialized.put("distances", distances);

            return serialized;
        }

        private IntConfigEntry createActivationDistance(Activation serverActivation) {
            return new IntConfigEntry(
                    serverActivation.getDefaultDistance(),
                    serverActivation.getMinDistance(),
                    serverActivation.getMaxDistance()
            );
        }
    }

    @Config
    @Data
    public static class Voice implements ClientConfig.Voice {

        @ConfigField
        private BooleanConfigEntry disabled = new BooleanConfigEntry(false);

        @ConfigField
        private BooleanConfigEntry microphoneDisabled = new BooleanConfigEntry(false);

        @ConfigField
        private DoubleConfigEntry activationThreshold = new DoubleConfigEntry(-30D, -60D, 0);

        @ConfigField
        private ConfigEntry<String> inputDevice = new ConfigEntry<>("");

        @ConfigField
        private ConfigEntry<String> outputDevice = new ConfigEntry<>("");

        @ConfigField
        private BooleanConfigEntry useJavaxInput = new BooleanConfigEntry(false);

        @ConfigField
        private DoubleConfigEntry microphoneVolume = new DoubleConfigEntry(1D, 0D, 2D);

        @ConfigField
        private BooleanConfigEntry noiseSuppression = new BooleanConfigEntry(false);

        @ConfigField
        private DoubleConfigEntry volume = new DoubleConfigEntry(1D, 0D, 2D);

        @ConfigField
        private BooleanConfigEntry compressorLimiter = new BooleanConfigEntry(true);

        @ConfigField
        private BooleanConfigEntry soundOcclusion = new BooleanConfigEntry(false);

        @ConfigField
        private BooleanConfigEntry directionalSources = new BooleanConfigEntry(false);

        @ConfigField
        private BooleanConfigEntry hrtf = new BooleanConfigEntry(false);

        @ConfigField
        private BooleanConfigEntry stereoCapture = new BooleanConfigEntry(false);

        @ConfigField
        private SourceLineVolumes volumes = new SourceLineVolumes();

        @Data
        public static class SourceLineVolumes implements SerializableConfigEntry, Volumes {

            private Map<String, DoubleConfigEntry> volumeByLineName = Maps.newHashMap();
            private Map<String, BooleanConfigEntry> muteByLineName = Maps.newHashMap();

            public SourceLineVolumes() {
            }

            @Override
            public synchronized void setVolume(@NotNull String lineName, double volume) {
                getVolume(lineName).set(volume);
            }

            @Override
            public synchronized DoubleConfigEntry getVolume(@NotNull String lineName) {
                return volumeByLineName.computeIfAbsent(
                        lineName,
                        (c) -> new DoubleConfigEntry(1D, 0D, 2D)
                );
            }

            @Override
            public synchronized void setMute(@NotNull String lineName, boolean muted) {
                getMute(lineName).set(muted);
            }

            @Override
            public synchronized BooleanConfigEntry getMute(@NotNull String lineName) {
                return muteByLineName.computeIfAbsent(
                        lineName,
                        (c) -> new BooleanConfigEntry(false)
                );
            }

            @Override
            public synchronized void deserialize(Object object) {
                try {
                    Map<String, Object> map = (Map<String, Object>) object;

                    map.forEach((key, serialized) -> {
                        Map<String, Object> value = (Map<String, Object>) serialized;

                        if (value.containsKey("volume")) {
                            setVolume(key, (double) value.get("volume"));
                        }

                        if (value.containsKey("muted")) {
                            setMute(key, (boolean) value.get("muted"));
                        }
                    });
                } catch (ClassCastException ignored) {
                }
            }

            @Override
            public synchronized Object serialize() {
                Map<String, Map<String, Object>> serialized = Maps.newHashMap();

                volumeByLineName.forEach((key, entry) -> {
                    Map<String, Object> value = Maps.newHashMap();
                    value.put("volume", entry.value());
                    serialized.put(key, value);
                });

                muteByLineName.forEach((key, entry) -> {
                    Map<String, Object> value = serialized.getOrDefault(key, Maps.newHashMap());

                    if (!entry.isDefault()) {
                        value.put("muted", entry.value());
                        serialized.put(key, value);
                    }
                });

                return serialized;
            }
        }
    }

    @Config
    @Data
    public static class Advanced implements ClientConfig.Advanced {

        @ConfigField
        private BooleanConfigEntry visualizeVoiceDistance = new BooleanConfigEntry(true);

        @ConfigField
        private BooleanConfigEntry visualizeVoiceDistanceOnJoin = new BooleanConfigEntry(false);

        @ConfigField
        private IntConfigEntry compressorThreshold = new IntConfigEntry(-10, -60, 0);

        @ConfigField
        private IntConfigEntry limiterThreshold = new IntConfigEntry(-6, -60, 0);

        @ConfigField
        private IntConfigEntry directionalSourcesAngle = new IntConfigEntry(145, 100, 360);

        @ConfigField
        private BooleanConfigEntry stereoSourcesToMono = new BooleanConfigEntry(false);

        @ConfigField
        private BooleanConfigEntry panning = new BooleanConfigEntry(true);

        @ConfigField
        private BooleanConfigEntry cameraSoundListener = new BooleanConfigEntry(true);

        @ConfigField
        private BooleanConfigEntry exponentialVolumeSlider = new BooleanConfigEntry(true);

        @ConfigField
        private BooleanConfigEntry exponentialDistanceGain = new BooleanConfigEntry(true);
    }

    @Config
    @Data
    public static class Overlay {

        @ConfigField
        private BooleanConfigEntry showActivationIcon = new BooleanConfigEntry(true);

        @ConfigField
        private EnumConfigEntry<IconPosition> activationIconPosition = new EnumConfigEntry<>(
                IconPosition.class,
                IconPosition.BOTTOM_CENTER
        );

        @ConfigField
        private IntConfigEntry showSourceIcons = new IntConfigEntry(0, 0, 2);

        @ConfigField
        private BooleanConfigEntry showStaticSourceIcons = new BooleanConfigEntry(true);


        @ConfigField
        private BooleanConfigEntry overlayEnabled = new BooleanConfigEntry(true);

        @ConfigField
        private EnumConfigEntry<OverlayPosition> overlayPosition = new EnumConfigEntry<>(
                OverlayPosition.class,
                OverlayPosition.TOP_LEFT
        );

        @ConfigField
        private EnumConfigEntry<OverlayStyle> overlayStyle = new EnumConfigEntry<>(
                OverlayStyle.class,
                OverlayStyle.NAME_SKIN
        );

        @ConfigField
        private SourceStates sourceStates = new SourceStates();

        @Data
        public static class SourceStates implements SerializableConfigEntry, ClientConfig.Overlay.SourceStates {

            private Map<String, EnumConfigEntry<OverlaySourceState>> stateByLineName = Maps.newHashMap();

            @Override
            public synchronized void setState(@NotNull String lineName,
                                              @NotNull OverlaySourceState state) {
                getState(lineName).set(state);
            }

            @Override
            public synchronized EnumConfigEntry<OverlaySourceState> getState(@NotNull String lineName) {
                return stateByLineName.computeIfAbsent(
                        lineName,
                        (c) -> new EnumConfigEntry<>(
                                OverlaySourceState.class,
                                OverlaySourceState.OFF
                        )
                );
            }

            public synchronized EnumConfigEntry<OverlaySourceState> getState(@NotNull SourceLine sourceLine) {
                return stateByLineName.computeIfAbsent(
                        sourceLine.getName(),
                        (c) -> new EnumConfigEntry<>(
                                OverlaySourceState.class,
                                sourceLine.hasPlayers()
                                        ? OverlaySourceState.WHEN_TALKING
                                        : OverlaySourceState.OFF
                        )
                );
            }

            @Override
            public synchronized void deserialize(Object object) {
                try {
                    Map<String, Object> map = (Map<String, Object>) object;

                    map.forEach((key, value) ->
                            setState(key, OverlaySourceState.valueOf((String) value))
                    );
                } catch (ClassCastException ignored) {
                }
            }

            @Override
            public synchronized Object serialize() {
                Map<String, String> serialized = Maps.newHashMap();

                stateByLineName.forEach((key, entry) ->
                        serialized.put(key, entry.value().name())
                );

                return serialized;
            }
        }
    }

    @Data
    public static class Addons implements SerializableConfigEntry {

        private final Map<String, Addon> addons = Maps.newHashMap();

        public synchronized @NotNull Addon getAddon(@NotNull String addonId) {
            return addons.computeIfAbsent(
                    addonId,
                    (aId) -> new Addon()
            );
        }

        @Override
        public synchronized void deserialize(Object object) {
            Map<String, Object> map = (Map<String, Object>) object;

            map.forEach((key, value) -> {
                Addon addon = new Addon();
                addon.deserialize(value);
                addons.put(key, addon);
            });
        }

        @Override
        public synchronized Object serialize() {
            Map<String, Object> serialized = Maps.newHashMap();

            addons.forEach((key, value) -> {
                if (value.entries.size() > 0) {
                    serialized.put(key, value.serialize());
                }
            });

            return serialized;
        }

        @Data
        public static class Addon implements SerializableConfigEntry {

            private final Map<String, ConfigEntry<?>> entries = Maps.newHashMap();

            public synchronized void setEntry(@NotNull String key, @NotNull ConfigEntry<?> entry) {
                entries.put(key, entry);
            }

            public synchronized Optional<ConfigEntry<?>> getEntry(@NotNull String key) {
                return Optional.ofNullable(entries.get(key));
            }

            @Override
            public synchronized void deserialize(Object object) {
                Map<String, Object> map = (Map<String, Object>) object;

                // what the fuck
                map.forEach((key, value) -> {
                    key = key.replaceAll("(^\")|(\"$)", "");

                    if (value instanceof Boolean) {
                        entries.put(key, new BooleanConfigEntry((Boolean) value));
                    } else if (value instanceof Long) {
                        entries.put(key, new IntConfigEntry(((Long) value).intValue(), 0, 0));
                    } else if (value instanceof Double) {
                        entries.put(key, new DoubleConfigEntry((Double) value, 0, 0));
                    } else if (value instanceof String) {
                        entries.put(key, new ConfigEntry<>(value));
                    }
                });
            }

            @Override
            public synchronized Object serialize() {
                Map<String, Object> serialized = Maps.newHashMap();

                entries.forEach((key, entry) -> serialized.put(key, entry.value()));

                return serialized;
            }
        }
    }
}
