package su.plo.voice.api.audio.device;

import java.util.HashMap;
import java.util.Map;

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
        return (T) params.get(key);
    }

    public boolean containsKey(String key) {
        return params.containsKey(key);
    }

    private void set(String key, Object param) {
        params.put(key, param);
    }

    public static class Builder {
        private final Params params = new Params();

        private Builder() {
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
