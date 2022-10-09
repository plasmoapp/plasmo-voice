package su.plo.voice.client.config.keybind;

import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.config.keybind.KeyBinding;

public final class KeyBindingConfigEntry extends ConfigEntry<KeyBinding> {

    public KeyBindingConfigEntry(KeyBinding defaultValue) {
        super(defaultValue);
    }

    @Override
    public void reset() {
        if (value == null) {
            this.value = ((VoiceKeyBinding) defaultValue).copy();
            return;
        }

        value.getKeys().clear();
        value.getKeys().addAll(this.defaultValue.getKeys());
    }

    @Override
    public void setDefault(KeyBinding value) {
        this.defaultValue = value;
        if (this.value == null) {
            this.value = ((VoiceKeyBinding) value).copy();
            this.changeListeners.forEach((listener) -> {
                listener.accept(value);
            });
        }
    }
}
