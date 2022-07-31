package su.plo.voice.api.client.event.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.source.AlSource;
import su.plo.voice.api.event.EventCancellable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link AlSource#play()} has been called
 */
public final class AlSourceUpdateParamEvent extends AlSourceEvent implements EventCancellable {

    @Getter
    private final int param;
    @Getter
    private final Object value;

    private boolean cancel;

    public AlSourceUpdateParamEvent(@NotNull AlSource source, int param, @NotNull Object value) {
        super(source);
        this.param = param;
        this.value = checkNotNull(value, "value cannot be null");
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
