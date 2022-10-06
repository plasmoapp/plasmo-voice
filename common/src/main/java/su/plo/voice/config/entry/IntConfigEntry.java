package su.plo.voice.config.entry;

import lombok.Getter;
import su.plo.config.entry.ConfigEntry;

public final class IntConfigEntry extends ConfigEntry<Integer> {

    @Getter
    private int min;
    @Getter
    private int max;

    public IntConfigEntry(int defaultValue, int min, int max) {
        super(defaultValue);
        this.min = min;
        this.max = max;
    }

    public void increment() {
        this.set((this.value() + 1) % (this.getMax() + 1));
    }

    public void decrement() {
        this.set(this.value() - 1 < this.getMin() ? this.getMax() : this.value() - 1);
    }

    @Override
    public void set(Integer value) {
        if (min != max && min > 0 && max > 0) {
            super.set(Math.max(Math.min(value, max), min));
        } else {
            super.set(value);
        }
    }

    public void setDefault(int value, int min, int max) {
        super.setDefault(value);
        this.min = min;
        this.max = max;
    }

    @Override
    public void deserialize(Object object) {
        if (object instanceof Long)  {
            super.deserialize(((Long) object).intValue());
        } else {
            super.deserialize(object);
        }
    }
}
