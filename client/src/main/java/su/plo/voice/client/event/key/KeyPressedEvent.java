package su.plo.voice.client.event.key;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.event.Event;

/**
 * This event is fires once the key was pressed
 */
public final class KeyPressedEvent implements Event {

    @Getter
    private final KeyBinding.Key key;
    @Getter
    private final KeyBinding.Action action;

    public KeyPressedEvent(@NonNull KeyBinding.Key key,
                           @NonNull KeyBinding.Action action) {
        this.key = key;
        this.action = action;
    }
}
