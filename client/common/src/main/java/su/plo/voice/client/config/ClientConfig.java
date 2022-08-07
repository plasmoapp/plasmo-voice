package su.plo.voice.client.config;

import com.google.common.collect.Maps;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.config.entry.DoubleConfigEntry;

import java.util.Map;
import java.util.Optional;

@Config
@Data
public final class ClientConfig {

    @ConfigField
    private Voice voice = new Voice();

    @ConfigField
    private ConfigKeyBindings keyBindings = new ConfigKeyBindings();

    @Config
    @Data
    public static class Voice {

        @ConfigField
        private ConfigEntry<Boolean> disabled = new ConfigEntry<>(false);

        @ConfigField
        private ConfigEntry<Boolean> microphoneDisabled = new ConfigEntry<>(false);

        @ConfigField
        private CategoryVolumes volumes = new CategoryVolumes();

        @Data
        public static class CategoryVolumes implements SerializableConfigEntry {

            private Map<String, DoubleConfigEntry> map = Maps.newHashMap();

            public CategoryVolumes() {
                setVolume("general", 1D);
                setVolume("priority", 1D);
            }

            public void setVolume(@NotNull String category, double volume) {
                map.put(category, new DoubleConfigEntry(volume, 0D, 2D));
            }

            public Optional<DoubleConfigEntry> getVolume(@NotNull String category) {
                return Optional.ofNullable(map.get(category));
            }

            @Override
            public void deserialize(Object object) {
                try {
                    Map<String, Object> map = (Map<String, Object>) object;
                    map.forEach((key, value) -> setVolume(key, (double) value));
                    System.out.println(map);
                } catch (ClassCastException ignored) {
                }
            }

            @Override
            public Object serialize() {
                Map<String, Double> serialized = Maps.newHashMap();
                this.map.forEach((key, entry) -> serialized.put(key, entry.value()));
                return serialized;
            }
        }
    }
}
