package su.plo.voice.client.event.key;

import lombok.Getter;
import su.plo.voice.api.event.EventCancellableBase;

/**
 * This event is fires once the key was pressed
 */
public class MouseScrollEvent extends EventCancellableBase {

    @Getter
    private final double horizontal;

    @Getter
    private final double vertical;

    public MouseScrollEvent(double horizontal, double vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
}
