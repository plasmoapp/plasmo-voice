package su.plo.voice.api.server.event.mute;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.mute.storage.MuteStorage;

/**
 * This event is fired once the mute storage is created, but not loaded yes
 * <br/>
 * You can replace the mute storage with yours
 */
@AllArgsConstructor
public final class MuteStorageCreateEvent implements Event {

    @Getter
    @Setter
    private @NonNull MuteStorage storage;
}
