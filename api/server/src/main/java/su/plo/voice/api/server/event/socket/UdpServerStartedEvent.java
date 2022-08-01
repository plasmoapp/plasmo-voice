package su.plo.voice.api.server.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the udp server is started
 */
public class UdpServerStartedEvent implements Event {

    @Getter
    private final UdpServer server;

    public UdpServerStartedEvent(@NotNull UdpServer server) {
        this.server = checkNotNull(server, "server cannot be null");
    }
}
