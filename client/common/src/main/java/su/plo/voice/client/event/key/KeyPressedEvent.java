package su.plo.voice.client.event.key;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fires once the key was pressed
 */
public class KeyPressedEvent implements Event {

    @Getter
    private final KeyBinding.Key key;

    @Getter
    private final KeyBinding.Action action;

    public KeyPressedEvent(@NotNull KeyBinding.Key key, @NotNull KeyBinding.Action action) {
        this.key = checkNotNull(key, "key");
        this.action = checkNotNull(action, "action");
    }
}
