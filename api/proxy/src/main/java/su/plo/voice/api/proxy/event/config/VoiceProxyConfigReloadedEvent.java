package su.plo.voice.api.proxy.event.config;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the server config is reloaded by /vreload
 */
public final class VoiceProxyConfigReloadedEvent implements Event {

    @Getter
    private final PlasmoVoiceProxy proxy;

    // todo: api server config?

    public VoiceProxyConfigReloadedEvent(@NotNull PlasmoVoiceProxy proxy) {
        this.proxy = checkNotNull(proxy, "proxy cannot be null");
    }
}
