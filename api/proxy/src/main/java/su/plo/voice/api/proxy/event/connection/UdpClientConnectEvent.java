package su.plo.voice.api.proxy.event.connection;

import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.socket.UdpProxyConnection;

/**
 * This event is fired once the player is successfully connected to the UDP server,
 * but not added to {@link UdpProxyConnectionManager} yet
 */
public final class UdpClientConnectEvent extends EventCancellableBase {

    @Getter
    private final UdpProxyConnection connection;

    public UdpClientConnectEvent(@NonNull UdpProxyConnection connection) {
        this.connection = connection;
    }
}
