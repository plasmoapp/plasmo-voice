package su.plo.voice.api.util;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// todo: doc & rename
public class Params {
    public static Params EMPTY = new Params();

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, Object> params = new HashMap<>();

    private Params() {
    }

    public <T> T get(String key) {
        if (!containsKey(key)) throw new IllegalArgumentException(key + " cannot be null");
        try {
            return (T) params.get(key);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(key + " is incorrect");
        }
    }

    public <T> T get(String key, Class<?> type) {
        if (!containsKey(key)) throw new IllegalArgumentException(key + " cannot be null");
        try {
            return (T) params.get(key);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(key + " is not " + type.getName());
        }
    }

    public boolean containsKey(String key) {
        return params.containsKey(key);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return params.entrySet();
    }

    public Map<String, String> toStringMap() {
        Map<String, String> map = Maps.newHashMap();
        params.forEach((key, value) -> map.put(key, value.toString()));
        return map;
    }

    public Map<String, Object> toMap() {
        return Maps.newHashMap(params);
    }

    private void set(String key, Object param) {
        params.put(key, param);
    }

    public static class Builder {
        private final Params params = new Params();

        private Builder() {
        }

        public Builder putAll(Map<String, ?> params) {
            params.forEach(this::set);
            return this;
        }

        public Builder set(String key, Object param) {
            params.set(key, param);
            return this;
        }

        public Params build() {
            return params;
        }
    }
}
