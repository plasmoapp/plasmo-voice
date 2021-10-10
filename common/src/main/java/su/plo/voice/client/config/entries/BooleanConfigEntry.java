package su.plo.voice.client.config.entries;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BooleanConfigEntry extends ConfigEntry<Boolean> implements JsonDeserializer<BooleanConfigEntry>,
        JsonSerializer<BooleanConfigEntry> {
    public void invert() {
        this.set(!this.get());
    }

    @Override
    public BooleanConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BooleanConfigEntry entry = new BooleanConfigEntry();
        try {
            entry.set(json.getAsBoolean());
        } catch (UnsupportedOperationException ignored) {}
        return entry;
    }

    @Override
    public JsonElement serialize(BooleanConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
        return src.get() == null ? null : new JsonPrimitive(src.get());
    }
}