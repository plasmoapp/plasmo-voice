package su.plo.voice.api.proxy.event.connection;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;

/**
 * This event is fired once the player is disconnected from the UDP server
 * and removed from {@link UdpProxyConnectionManager}
 */
public final class UdpClientDisconnectEvent implements Event {

    @Getter
    private final UdpProxyConnection connection;

    public UdpClientDisconnectEvent(@NonNull UdpProxyConnection connection) {
        this.connection = connection;
    }
}
