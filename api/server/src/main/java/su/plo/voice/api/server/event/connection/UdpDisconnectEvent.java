package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.socket.UdpConnection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the player is disconnected from the UDP server
 * and removed from {@link UdpServerConnectionManager}
 */
public final class UdpDisconnectEvent implements Event {

    @Getter
    private final UdpConnection connection;

    public UdpDisconnectEvent(@NotNull UdpConnection connection) {
        this.connection = checkNotNull(connection, "connection cannot be null");
    }
}
