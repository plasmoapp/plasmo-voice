package su.plo.voice.api.server.event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the server config is loaded
 */
public final class VoiceServerConfigLoadedEvent implements Event {

    @Getter
    private final PlasmoVoiceServer server;

    // todo: api server config?

    public VoiceServerConfigLoadedEvent(@NotNull PlasmoVoiceServer server) {
        this.server = checkNotNull(server, "server cannot be null");
    }
}
