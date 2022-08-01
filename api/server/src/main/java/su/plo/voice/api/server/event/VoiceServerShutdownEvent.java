package su.plo.voice.api.server.event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the Plasmo Voice server is shutting down
 */
public class VoiceServerShutdownEvent implements Event {

    @Getter
    private final PlasmoVoiceServer server;

    public VoiceServerShutdownEvent(@NotNull PlasmoVoiceServer client) {
        this.server = checkNotNull(client, "client cannot be null");
    }
}
