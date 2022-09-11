package su.plo.voice.client.config.keybind;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceKeyBinding implements KeyBinding {

    private final Set<OnPress> onPress = new CopyOnWriteArraySet<>();

    private final KeyBindings keyBindings;
    @Getter
    private final String name;
    private final Set<Key> keys = new CopyOnWriteArraySet<>();
    @Getter
    private final boolean anyContext;

    public VoiceKeyBinding(@NotNull KeyBindings keyBindings, @NotNull String name, @NotNull List<Key> keys, boolean anyContext) {
        this.keyBindings = checkNotNull(keyBindings, "keyBindings");
        this.name = checkNotNull(name, "name");
        checkNotNull(keys, "keys");
        this.keys.addAll(keys);
        this.anyContext = anyContext;
    }

    @Getter
    private boolean pressed;

    @Override
    public Set<Key> getKeys() {
        return keys;
    }

    @Override
    public void resetState() {
        pressed = false;
    }

    @Override
    public void updateState(@NotNull Action keyAction) {
        if (getKeys().size() > 0) {
            if (keyAction == Action.DOWN && !pressed && keyBindings.getPressedKeys().containsAll(keys)) {
                this.pressed = true;
                onPress.forEach(action -> action.onPress(Action.DOWN));
            } else if (pressed && !keyBindings.getPressedKeys().containsAll(keys)) {
                this.pressed = false;
                onPress.forEach(action -> action.onPress(Action.UP));
            }
        }
    }

    @Override
    public void onPress(@NotNull OnPress onPress) {
        this.onPress.add(onPress);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof KeyBinding) {
            KeyBinding keyBinding = (KeyBinding) object;
            return this.hashCode() == keyBinding.hashCode();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, keys.hashCode());
    }
}
