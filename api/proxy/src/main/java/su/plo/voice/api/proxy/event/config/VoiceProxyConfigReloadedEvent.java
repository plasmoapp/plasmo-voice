package su.plo.voice.api.proxy.event.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.config.ProxyConfig;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the proxy config is reloaded via /vreload.
 */
@RequiredArgsConstructor
public final class VoiceProxyConfigReloadedEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceProxy proxy;
    @Getter
    private final @NonNull ProxyConfig config;
}
