package su.plo.voice.api.server.event.socket;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

/**
 * This event is fired once the UDP server is started.
 */
@RequiredArgsConstructor
public class UdpServerStartedEvent implements Event {

    @Getter
    private final @NonNull UdpServer server;
}
