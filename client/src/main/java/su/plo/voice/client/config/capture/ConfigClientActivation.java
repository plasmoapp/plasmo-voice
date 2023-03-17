package su.plo.voice.client.config.capture;

import lombok.Data;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.voice.api.client.audio.capture.ClientActivation;

@Config
@Data
public final class ConfigClientActivation {

    @ConfigField(path = "type")
    private EnumConfigEntry<ClientActivation.Type> configType = new EnumConfigEntry<>(
            ClientActivation.Type.class,
            ClientActivation.Type.PUSH_TO_TALK
    );

    @ConfigField(path = "toggle")
    private BooleanConfigEntry configToggle = new BooleanConfigEntry(false);

    public boolean isDefault() {
        return configType.isDefault()
                && configToggle.isDefault();
    }
}
