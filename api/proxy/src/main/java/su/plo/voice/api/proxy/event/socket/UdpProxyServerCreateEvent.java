package su.plo.voice.api.proxy.event.socket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.socket.UdpProxyServer;

/**
 * This event is fired when the UDP proxy server is created but has not started yet.
 * <p>
 * You can replace the default server with your custom implementation.
 * The default server implementation is netty.
 */
@AllArgsConstructor
public final class UdpProxyServerCreateEvent implements Event {

    @Getter
    private @NonNull UdpProxyServer server;
}
