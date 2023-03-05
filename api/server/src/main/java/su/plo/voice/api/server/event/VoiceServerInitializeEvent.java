package su.plo.voice.api.server.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

/**
 * This event is fired when the Plasmo Voice server is initializing
 */
@RequiredArgsConstructor
public final class VoiceServerInitializeEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceServer server;
}
