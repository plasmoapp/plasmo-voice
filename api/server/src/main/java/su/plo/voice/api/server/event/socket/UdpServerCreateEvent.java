package su.plo.voice.api.server.event.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

/**
 * This event is fired when the UDP server is created but has not started yet.
 * <p>
 * You can replace the default server with your custom implementation.
 * The default server implementation is netty.
 */
@AllArgsConstructor
public class UdpServerCreateEvent implements Event {

    @Getter
    @Setter
    private @NonNull UdpServer server;
}
