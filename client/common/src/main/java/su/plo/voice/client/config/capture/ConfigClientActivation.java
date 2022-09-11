package su.plo.voice.client.config.capture;

import lombok.Data;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.config.entry.IntConfigEntry;

@Config
@Data
public final class ConfigClientActivation {

    @ConfigField(path = "distance")
    private IntConfigEntry configDistance = new IntConfigEntry(0, 0, Short.MAX_VALUE);
    @ConfigField(path = "type")
    private EnumConfigEntry<ClientActivation.Type> configType = new EnumConfigEntry<>(
            ClientActivation.Type.class,
            ClientActivation.Type.PUSH_TO_TALK
    );

    @ConfigField(path = "toggle")
    private ConfigEntry<Boolean> configToggle = new ConfigEntry<>(false);

    public boolean isDefault() {
        return configDistance.isDefault()
                && configType.isDefault()
                && configToggle.isDefault();
    }
}
