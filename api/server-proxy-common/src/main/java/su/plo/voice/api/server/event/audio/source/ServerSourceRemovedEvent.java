package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.source.ServerAudioSource;

/**
 * This event is fired once a new source is removed.
 */
@RequiredArgsConstructor
public final class ServerSourceRemovedEvent extends EventCancellableBase {

    @Getter
    private final ServerAudioSource<?> source;
}
