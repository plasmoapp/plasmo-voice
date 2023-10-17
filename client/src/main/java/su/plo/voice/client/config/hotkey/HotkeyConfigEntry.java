package su.plo.voice.client.config.hotkey;

import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.config.hotkey.Hotkey;

public final class HotkeyConfigEntry extends ConfigEntry<Hotkey> {

    public HotkeyConfigEntry(Hotkey defaultValue) {
        super(defaultValue);
    }

    @Override
    public void reset() {
        if (value == null) {
            this.value = ((VoiceHotkey) defaultValue).copy();
            return;
        }

        value.setKeys(this.defaultValue.getKeys());
    }

    @Override
    public void setDefault(Hotkey value) {
        this.defaultValue = value;
        if (this.value == null) {
            this.value = ((VoiceHotkey) value).copy();
            this.changeListeners.forEach((listener) -> {
                listener.accept(value);
            });
        }
    }
}
