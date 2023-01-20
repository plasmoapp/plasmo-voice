package su.plo.voice.api.server.event.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.capture.ServerActivation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired right before {@link ServerActivation} is unregistered
 */
public final class ServerActivationUnregisterEvent extends EventCancellableBase {

    @Getter
    private final ServerActivation activation;

    public ServerActivationUnregisterEvent(@NotNull ServerActivation activation) {
        this.activation = checkNotNull(activation, "activation cannot be null");
    }
}
