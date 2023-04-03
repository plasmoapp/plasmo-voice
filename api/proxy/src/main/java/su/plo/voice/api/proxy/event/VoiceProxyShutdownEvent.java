package su.plo.voice.api.proxy.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;

/**
 * This event is fired when the Plasmo Voice server is shutting down
 *
 * @deprecated use {@link AddonInitializer#onAddonShutdown()}
 */
@Deprecated
@RequiredArgsConstructor
public final class VoiceProxyShutdownEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceProxy proxy;
}
