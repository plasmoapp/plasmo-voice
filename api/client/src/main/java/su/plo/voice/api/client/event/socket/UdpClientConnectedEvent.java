package su.plo.voice.api.client.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventCancellableBase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the udp client is connected to a server
 */
public class UdpClientConnectedEvent extends EventCancellableBase {

    @Getter
    private final UdpClient client;

    public UdpClientConnectedEvent(@NotNull UdpClient client) {
        this.client = checkNotNull(client, "server cannot be null");
    }
}
