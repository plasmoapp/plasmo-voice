package su.plo.voice.config.entries;

import java.util.Objects;

public class ConfigEntry<E> {
    protected transient E defaultValue = null;
    protected E value = null;

    public ConfigEntry() {
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public boolean isDefault() {
        return Objects.equals(defaultValue, value);
    }

    public void set(E value) {
        this.value = value;
    }

    public void setDefault(E value) {
        this.defaultValue = value;
        if (this.value == null) {
            this.value = value;
        }
    }

    public E getDefault() {
        return defaultValue;
    }

    public E get() {
        return value;
    }
}