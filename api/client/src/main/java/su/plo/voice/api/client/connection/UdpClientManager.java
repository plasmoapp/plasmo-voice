package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.socket.UdpClient;

import java.util.Optional;

public interface UdpClientManager {

    /**
     * Sets the {@link UdpClient}
     *
     * @param client {@link UdpClient}
     */
    void setClient(@NotNull UdpClient client);

    /**
     * Disconnects and removes an udp client from the manager
     *
     * @param reason disconnect reason
     */
    void removeClient(@NotNull UdpClientClosedEvent.Reason reason);

    /**
     * Gets the {@link UdpClient}
     *
     * @return {@link UdpClient}
     */
    Optional<UdpClient> getClient();
}
