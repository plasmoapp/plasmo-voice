package su.plo.voice.api.client.config.keybind;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public interface KeyBinding {

    @NotNull String getName();

    Collection<Key> getKeys();

    boolean isAnyContext();

    boolean isPressed();

    void resetState();

    void updateState(@NotNull Action action);

    void onPress(@NotNull OnPress onPress);

    @FunctionalInterface
    interface OnPress {
        void onPress(@NotNull Action action);
    }

    enum Action {
        UP,
        DOWN,
        UNKNOWN;

        public static Action fromInt(int action) {
            return action == 0
                    ? KeyBinding.Action.UP
                    : action == 1
                    ? KeyBinding.Action.DOWN
                    : KeyBinding.Action.UNKNOWN;
        }
    }

    enum Type {
        KEYSYM,
        SCANCODE,
        MOUSE;

        private final Map<Integer, Key> map = Maps.newHashMap();

        public Key getOrCreate(int keyCode) {
            return map.computeIfAbsent(keyCode, (k) -> new Key(this, keyCode));
        }
    }

    @AllArgsConstructor
    final class Key {
        @Getter
        private final @NotNull Type type;
        @Getter
        private final int code;

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            } else if (object != null && this.getClass() == object.getClass()) {
                Key key = (Key) object;
                return this.code == key.code && this.type == key.type;
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.type, this.code);
        }
    }
}
