package su.plo.voice.api.client.event.socket;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the udp client is created, but not connected yet
 *
 * You can replace a client with yours
 * Default server is a netty udp client
 */
public final class UdpClientConnectEvent extends EventCancellableBase {

    @Getter
    @Setter
    private UdpClient client;

    @Getter
    private final ConnectionPacket connectionPacket;

    public UdpClientConnectEvent(@NotNull UdpClient client, @NotNull ConnectionPacket connectionPacket) {
        this.client = checkNotNull(client, "client cannot be null");
        this.connectionPacket = checkNotNull(connectionPacket, "connectionPacket cannot be null");
    }
}
