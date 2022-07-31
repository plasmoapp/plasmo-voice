package su.plo.voice.api.server.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the udp server is started
 */
public class UdpServerStartEvent implements Event {

    @Getter
    private final UdpServer server;

    public UdpServerStartEvent(@NotNull UdpServer server) {
        checkNotNull(server, "server cannot be null");
        this.server = server;
    }
}
