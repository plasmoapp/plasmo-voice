package su.plo.voice.api.event.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AlSource;

/**
 * This event is called once the {@link AlSource} has been created
 */
public final class AlSourceCreatedEvent extends AlSourceEvent {

    public AlSourceCreatedEvent(@NotNull AlSource source) {
        super(source);
    }
}
