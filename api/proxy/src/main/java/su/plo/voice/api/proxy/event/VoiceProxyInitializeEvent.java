package su.plo.voice.api.proxy.event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the Plasmo Voice proxy is initializing
 */
public final class VoiceProxyInitializeEvent implements Event {

    @Getter
    private final PlasmoVoiceProxy proxy;

    public VoiceProxyInitializeEvent(@NotNull PlasmoVoiceProxy proxy) {
        this.proxy = checkNotNull(proxy, "server cannot be null");
    }
}
