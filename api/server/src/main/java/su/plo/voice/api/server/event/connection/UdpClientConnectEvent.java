package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.socket.UdpServerConnection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the player is successfully connected to the UDP server,
 * but not added to {@link UdpServerConnectionManager} yet
 */
public final class UdpClientConnectEvent extends EventCancellableBase {

    @Getter
    private final UdpServerConnection connection;

    public UdpClientConnectEvent(@NotNull UdpServerConnection connection) {
        this.connection = checkNotNull(connection, "connection cannot be null");
    }
}
