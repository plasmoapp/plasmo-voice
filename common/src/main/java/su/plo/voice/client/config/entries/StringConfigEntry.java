package su.plo.voice.client.config.entries;

import com.google.gson.*;

import java.lang.reflect.Type;

public class StringConfigEntry extends ConfigEntry<String> implements JsonDeserializer<StringConfigEntry>,
        JsonSerializer<StringConfigEntry> {
    @Override
    public StringConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        StringConfigEntry entry = new StringConfigEntry();
        try {
            entry.set(json.getAsString());
        } catch (UnsupportedOperationException ignored) {}
        return entry;
    }

    @Override
    public JsonElement serialize(StringConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
        return src.get() == null ? null : new JsonPrimitive(src.get());
    }
}