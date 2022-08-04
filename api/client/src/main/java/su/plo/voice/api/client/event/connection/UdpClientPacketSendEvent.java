package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link UdpClient}
 * is about to send packet to UDP server
 */
public final class UdpClientPacketSendEvent extends EventCancellableBase {

    @Getter
    private final UdpClient client;

    @Getter
    @Setter
    private Packet<?> packet;

    public UdpClientPacketSendEvent(@NotNull UdpClient client, @NotNull Packet<?> packet) {
        this.client = checkNotNull(client, "client cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
