package su.plo.voice.api.client.event.audio.device.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.event.EventCancellable;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link AlSource#write(byte[])} has been invoked.
 */
public final class AlSourceWriteEvent extends AlSourceEvent implements EventCancellable {

    @Getter
    private final ByteBuffer buffer;

    private boolean cancel;

    public AlSourceWriteEvent(@NotNull AlSource source, @NotNull ByteBuffer buffer) {
        super(source);
        this.buffer = checkNotNull(buffer, "buffer cannot be null");
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
