package su.plo.voice.client.config.entries;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IntegerListConfigEntry extends ConfigEntry<List<Integer>> implements JsonDeserializer<IntegerListConfigEntry>,
        JsonSerializer<IntegerListConfigEntry> {
    public IntegerListConfigEntry() {
    }

    @Override
    public IntegerListConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        IntegerListConfigEntry entry = new IntegerListConfigEntry();
        List<Integer> list = new ArrayList<>();
        try {
            for (JsonElement el : json.getAsJsonArray()) {
                list.add(el.getAsInt());
            }
        } catch (UnsupportedOperationException ignored) {}
        entry.set(list);
        return entry;
    }

    @Override
    public JsonElement serialize(IntegerListConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray list = new JsonArray();
        for (Integer i : src.get()) {
            list.add(i);
        }

        return src.get() == null ? null : list;
    }
}