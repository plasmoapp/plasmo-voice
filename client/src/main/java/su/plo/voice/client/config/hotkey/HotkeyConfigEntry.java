package su.plo.voice.client.config.hotkey;

import su.plo.config.entry.ConfigEntry;
import su.plo.voice.api.client.config.hotkey.Hotkey;

import java.util.Set;

public final class HotkeyConfigEntry extends ConfigEntry<Hotkey> {

    public HotkeyConfigEntry(Hotkey defaultValue) {
        super(defaultValue);
    }

    public void updateKeys(Set<Hotkey.Key> newKeys) {
        value.setKeys(newKeys);
        triggerListeners();
    }

    @Override
    public void reset() {
        if (value == null) {
            this.value = ((VoiceHotkey) defaultValue).copy();
            triggerListeners();
            return;
        }

        updateKeys(this.defaultValue.getKeys());
    }

    @Override
    public void setDefault(Hotkey value) {
        this.defaultValue = value;
        if (this.value == null) {
            this.value = ((VoiceHotkey) value).copy();
            triggerListeners();
        }
    }

    private void triggerListeners() {
        changeListeners.forEach((listener) -> listener.accept(this.value));
    }
}
