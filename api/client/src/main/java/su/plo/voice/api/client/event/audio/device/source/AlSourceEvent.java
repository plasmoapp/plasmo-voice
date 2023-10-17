package su.plo.voice.api.client.event.audio.device.source;

import lombok.Getter;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.event.Event;

/**
 * Base event for AL sources.
 */
abstract class AlSourceEvent implements Event {

    @Getter
    protected final AlSource source;

    protected AlSourceEvent(AlSource source) {
        this.source = source;
    }
}
