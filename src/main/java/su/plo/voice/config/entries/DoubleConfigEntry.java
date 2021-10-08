package su.plo.voice.config.entries;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;

public class DoubleConfigEntry extends ConfigEntry<Double> implements JsonDeserializer<DoubleConfigEntry>,
        JsonSerializer<DoubleConfigEntry> {
    @Getter
    private double min;
    @Getter
    private double max;

    public DoubleConfigEntry(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Double value) {
        if (min != max && min > 0) {
            super.set(Math.max(Math.min(value, max), min));
        } else {
            super.set(value);
        }
    }

    public void setDefault(double value, double min, double max) {
        super.setDefault(value);
        this.min = min;
        this.max = max;
    }

    @Override
    public DoubleConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        DoubleConfigEntry entry = new DoubleConfigEntry(0, 0);
        try {
            entry.set(json.getAsDouble());
        } catch (UnsupportedOperationException ignored) {}
        return entry;
    }

    @Override
    public JsonElement serialize(DoubleConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
        return src.get() == null ? null : new JsonPrimitive(src.get());
    }
}