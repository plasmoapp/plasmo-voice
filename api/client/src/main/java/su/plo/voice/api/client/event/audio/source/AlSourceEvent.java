package su.plo.voice.api.client.event.audio.source;

import lombok.Getter;
import su.plo.voice.api.client.audio.source.AlSource;
import su.plo.voice.api.event.Event;

abstract class AlSourceEvent implements Event {

    @Getter
    protected final AlSource source;

    protected AlSourceEvent(AlSource source) {
        this.source = source;
    }
}
