package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.socket.UdpServerConnection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the player is successfully connected to the UDP server
 * and added to {@link UdpServerConnectionManager}
 */
public final class UdpClientConnectedEvent implements Event {

    @Getter
    private final UdpServerConnection connection;

    public UdpClientConnectedEvent(@NotNull UdpServerConnection connection) {
        this.connection = checkNotNull(connection, "connection cannot be null");
    }
}
