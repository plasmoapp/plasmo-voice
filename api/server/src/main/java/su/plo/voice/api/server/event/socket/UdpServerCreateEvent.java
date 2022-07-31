package su.plo.voice.api.server.event.socket;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the udp server is created, but not started yet
 *
 * You can replace a server with yours
 * Default server is a netty udp server
 */
public class UdpServerCreateEvent extends EventCancellableBase {

    @Getter
    @Setter
    private UdpServer server;

    public UdpServerCreateEvent(@NotNull UdpServer server) {
        checkNotNull(server, "server cannot be null");
        this.server = server;
    }
}
