package su.plo.voice.api.server.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the udp server is stopped
 */
public class UdpServerStoppedEvent implements Event {

    @Getter
    private final UdpServer server;

    public UdpServerStoppedEvent(@NotNull UdpServer server) {
        this.server = checkNotNull(server, "server cannot be null");
    }
}
