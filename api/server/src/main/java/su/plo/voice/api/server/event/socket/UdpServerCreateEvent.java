package su.plo.voice.api.server.event.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

/**
 * This event is fired once the udp server is created, but not started yet
 * <br/>
 * You can replace a server with yours
 * Default server is a netty udp server
 */
@AllArgsConstructor
public class UdpServerCreateEvent implements Event {

    @Getter
    @Setter
    private @NonNull UdpServer server;
}
