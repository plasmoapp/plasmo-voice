package su.plo.voice.api.server.event.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.config.ServerConfig;

/**
 * This event is fired once the server config is reloaded via /vreload.
 */
@RequiredArgsConstructor
public final class VoiceServerConfigReloadedEvent implements Event {

    @Getter
    private final @NonNull PlasmoVoiceServer server;
    @Getter
    private final @NonNull ServerConfig config;
}
