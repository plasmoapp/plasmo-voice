package su.plo.voice.api.event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoiceClient;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is called once the Plasmo Voice client is shutdown
 */
public final class VoiceClientShutdownEvent implements Event {

    @Getter
    private final PlasmoVoiceClient client;

    public VoiceClientShutdownEvent(@NotNull PlasmoVoiceClient client) {
        this.client = checkNotNull(client, "client cannot be null");
    }
}
