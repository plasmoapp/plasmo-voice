package su.plo.voice.api.proxy.event.connection;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;

/**
 * This event is fired once the player is successfully connected to the UDP server
 * and added to {@link UdpProxyConnectionManager}
 */
public final class UdpClientConnectedEvent implements Event {

    @Getter
    private final UdpProxyConnection connection;

    public UdpClientConnectedEvent(@NonNull UdpProxyConnection connection) {
        this.connection = connection;
    }
}
