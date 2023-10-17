package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.socket.UdpClient;

import java.util.Optional;

/**
 * Manages the current UDP client.
 */
public interface UdpClientManager {

    /**
     * Sets the {@link UdpClient}
     *
     * @param client {@link UdpClient}
     */
    void setClient(@NotNull UdpClient client);

    /**
     * Disconnects and removes the UDP client from the manager.
     *
     * @param reason The reason for disconnecting.
     */
    void removeClient(@NotNull UdpClientClosedEvent.Reason reason);

    /**
     * Gets the {@link UdpClient}, if available.
     *
     * @return An optional containing the {@link UdpClient}, or empty if not set.
     */
    Optional<UdpClient> getClient();

    /**
     * Checks if the UDP client is present, connected, and not timed out.
     *
     * @return {@code true} if the UDP client is connected and not timed out, {@code false} otherwise.
     */
    boolean isConnected();
}
