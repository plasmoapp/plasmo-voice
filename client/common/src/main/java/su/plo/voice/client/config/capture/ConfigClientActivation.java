package su.plo.voice.client.config.capture;

import lombok.Data;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.config.entry.IntConfigEntry;

@Config
@Data
public class ConfigClientActivation {

    @ConfigField(path = "distance")
    protected IntConfigEntry configDistance = new IntConfigEntry(0, 0, Short.MAX_VALUE);
    @ConfigField(path = "type")
    protected EnumConfigEntry<ClientActivation.Type> configType = new EnumConfigEntry<>(
            ClientActivation.Type.class,
            ClientActivation.Type.PUSH_TO_TALK
    );

    public boolean isDefault() {
        return configDistance.isDefault() && configType.isDefault();
    }
}
