package su.plo.voice.client.config.entries;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;

public class IntegerConfigEntry extends ConfigEntry<Integer> implements JsonDeserializer<IntegerConfigEntry>,
        JsonSerializer<IntegerConfigEntry> {
    @Getter
    private int min;
    @Getter
    private int max;

    public IntegerConfigEntry() {
    }

    public IntegerConfigEntry(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void increment() {
        this.set((this.get() + 1) % (this.getMax() + 1));
    }

    public void decrement() {
        this.set(this.get() - 1 < this.getMin() ? this.getMax() : this.get() - 1);
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
    public IntegerConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        IntegerConfigEntry entry = new IntegerConfigEntry(0, 0);
        try {
            entry.set(json.getAsInt());
        } catch (UnsupportedOperationException ignored) {}
        return entry;
    }

    @Override
    public JsonElement serialize(IntegerConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
        return src.get() == null ? null : new JsonPrimitive(src.get());
    }
}