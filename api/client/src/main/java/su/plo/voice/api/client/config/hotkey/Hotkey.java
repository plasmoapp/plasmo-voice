package su.plo.voice.api.client.config.hotkey;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a hotkey.
 *
 * @see Hotkeys
 */
public interface Hotkey {

    /**
     * Gets the name of the hotkey.
     *
     * @return The name of the hotkey.
     */
    @NotNull String getName();

    /**
     * Gets the set of keys associated with this hotkey.
     *
     * @return The set of keys associated with the hotkey.
     */
    Set<Key> getKeys();

    /**
     * Sets the keys associated with this hotkey.
     *
     * @param newKeys The new set of keys for the hotkey.
     */
    void setKeys(@NotNull Set<Key> newKeys);

    /**
     * Checks if the hotkey is active in any context.
     * <br>
     * For example, the hotkey can be active in any menu.
     *
     * @return {@code true} if the hotkey is active in any context, {@code false} otherwise.
     */
    boolean isAnyContext();

    /**
     * Checks if the hotkey is currently pressed.
     *
     * @return {@code true} if the hotkey is currently pressed, {@code false} otherwise.
     */
    boolean isPressed();

    /**
     * Resets the pressed state of the hotkey.
     */
    void resetState();

    /**
     * Updates the pressed state of the hotkey based on a given action.
     *
     * @param action The action that triggered the state change.
     */
    void updateState(@NotNull Action action);

    /**
     * Adds a listener to be notified when the hotkey is pressed.
     *
     * @param onPress The listener to add.
     */
    void addPressListener(@NotNull OnPress onPress);

    /**
     * Removes a press listener.
     *
     * @param onPress The listener to remove.
     */
    void removePressListener(@NotNull OnPress onPress);

    /**
     * Clears all press listeners.
     */
    void clearPressListener();

    /**
     * Adds a listener to be notified when the {@link #getKeys()} change.
     *
     * @param onKeysChange The listener to add.
     */
    void addKeysChangeListener(@NotNull OnKeysChange onKeysChange);

    /**
     * Removes a keys change listener.
     *
     * @param onKeysChange The listener to remove.
     */
    void removeKeysChangeListener(@NotNull OnKeysChange onKeysChange);

    /**
     * Clears all keys change listeners.
     */
    void clearKeysChangeListeners();

    @FunctionalInterface
    interface OnPress {

        /**
         * Invoked when the hotkey is pressed.
         *
         * @param action The action associated with the press.
         */
        void onPress(@NotNull Action action);
    }

    @FunctionalInterface
    interface OnKeysChange {

        /**
         * Invoked when the keys associated with the hotkey change.
         *
         * @param newKeys The new set of keys for the hotkey.
         */
        void onKeysChange(@NotNull Set<Key> newKeys);
    }

    enum Action {

        UP,
        DOWN,
        UNKNOWN;

        public static Action fromInt(int action) {
            return action == 0
                    ? Hotkey.Action.UP
                    : action == 1
                    ? Hotkey.Action.DOWN
                    : Hotkey.Action.UNKNOWN;
        }
    }

    enum Type {

        KEYSYM,
        SCANCODE,
        MOUSE;

        private final Map<Integer, Key> keys = Maps.newHashMap();

        public Key getOrCreate(int keyCode) {
            return keys.computeIfAbsent(keyCode, (k) -> new Key(this, keyCode));
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
