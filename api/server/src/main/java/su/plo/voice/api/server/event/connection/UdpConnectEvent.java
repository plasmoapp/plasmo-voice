package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpConnection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the player is successfully connected to the UDP server,
 * but not added to {@link su.plo.voice.api.server.connection.ConnectionManager} yet
 */
public final class UdpConnectEvent extends EventCancellableBase {

    @Getter
    private final UdpConnection connection;

    public UdpConnectEvent(@NotNull UdpConnection connection) {
        checkNotNull(connection, "connection cannot be null");
        this.connection = connection;
    }
}
