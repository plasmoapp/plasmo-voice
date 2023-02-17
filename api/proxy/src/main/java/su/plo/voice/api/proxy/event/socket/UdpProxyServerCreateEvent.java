package su.plo.voice.api.proxy.event.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.socket.UdpProxyServer;

/**
 * This event is fired once the udp proxy server is created, but not started yet
 * <p>
 * You can replace a server with yours
 * Default server is a netty udp server
 */
@AllArgsConstructor
public final class UdpProxyServerCreateEvent implements Event {

    @Getter
    private @NonNull UdpProxyServer server;
}
