package su.plo.voice.client.config.hotkey;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.config.hotkey.Hotkeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceHotkey implements Hotkey {

    private final Set<OnPress> onPress = new CopyOnWriteArraySet<>();
    private final Set<OnKeysChange> onKeysChange = new CopyOnWriteArraySet<>();

    private final Hotkeys keyBindings;
    @Getter
    private final String name;
    private final Set<Key> keys = new CopyOnWriteArraySet<>();
    @Getter
    private final boolean anyContext;

    public VoiceHotkey(@NotNull Hotkeys keyBindings, @NotNull String name, @NotNull List<Key> keys, boolean anyContext) {
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
    public void setKeys(@NotNull Set<Key> newKeys) {
        keys.clear();
        keys.addAll(newKeys);
        onKeysChange.forEach(action -> action.onKeysChange(newKeys));
        resetState();
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
    public void addPressListener(@NotNull OnPress onPress) {
        this.onPress.add(onPress);
    }

    @Override
    public void removePressListener(@NotNull OnPress onPress) {
        this.onPress.remove(onPress);
    }

    @Override
    public void clearPressListener() {
        onPress.clear();
    }

    @Override
    public void addKeysChangeListener(@NotNull OnKeysChange onKeysChange) {
        this.onKeysChange.add(onKeysChange);
    }

    @Override
    public void removeKeysChangeListener(@NotNull OnKeysChange onKeysChange) {
        this.onKeysChange.remove(onKeysChange);
    }

    public @NotNull VoiceHotkey copy() {
        return new VoiceHotkey(keyBindings, name, new ArrayList<>(keys), anyContext);
    }

    @Override
    public void clearKeysChangeListeners() {
        onKeysChange.clear();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof Hotkey) {
            Hotkey keyBinding = (Hotkey) object;
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
