package su.plo.voice.api.server.event.mute;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.mute.storage.MuteStorage;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the mute storage is created, but not loaded yes
 *
 * You can replace a server with yours
 */
public final class MuteStorageCreateEvent implements Event {

    @Getter
    private MuteStorage storage;

    public MuteStorageCreateEvent(@NotNull MuteStorage storage) {
        this.storage = checkNotNull(storage, "storage cannot be null");
    }

    public void setStorage(@NotNull MuteStorage storage) {
        this.storage = checkNotNull(storage, "storage cannot be null");
    }
}
