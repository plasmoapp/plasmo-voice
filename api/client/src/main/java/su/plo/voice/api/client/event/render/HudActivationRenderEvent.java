package su.plo.voice.api.client.event.render;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the client is about to render the activation in the HUD
 */
public final class HudActivationRenderEvent implements Event {

    @Getter
    private final ClientActivation activation;
    @Getter
    @Setter
    private boolean render;

    public HudActivationRenderEvent(@NotNull ClientActivation activation, boolean render) {
        this.activation = checkNotNull(activation, "activation cannot be null");
        this.render = render;
    }
}
