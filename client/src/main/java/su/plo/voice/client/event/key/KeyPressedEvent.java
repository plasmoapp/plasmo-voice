package su.plo.voice.client.event.key;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.event.Event;

/**
 * This event is fires once the key was pressed
 */
public final class KeyPressedEvent implements Event {

    @Getter
    private final Hotkey.Key key;
    @Getter
    private final Hotkey.Action action;

    public KeyPressedEvent(@NonNull Hotkey.Key key,
                           @NonNull Hotkey.Action action) {
        this.key = key;
        this.action = action;
    }
}
