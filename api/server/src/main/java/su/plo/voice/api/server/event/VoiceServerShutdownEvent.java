package su.plo.voice.api.server.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.addon.AddonInitializer;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;

/**
 * This event is fired when the Plasmo Voice server is shutting down
 *
 * @deprecated use {@link AddonInitializer#onAddonShutdown()}
 */
@RequiredArgsConstructor
@Deprecated
public final class VoiceServerShutdownEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceServer server;
}
