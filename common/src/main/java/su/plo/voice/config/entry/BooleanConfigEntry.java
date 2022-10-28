package su.plo.voice.config.entry;

import su.plo.config.entry.ConfigEntry;

public final class BooleanConfigEntry extends ConfigEntry<Boolean> {

    public BooleanConfigEntry(Boolean defaultValue) {
        super(defaultValue);
    }

    public void invert() {
        set(!value);
    }
}
