package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link su.plo.voice.api.server.socket.UdpServer}
 * is about to send packet to {@link UdpServerConnection}
 */
public final class UdpPacketSendEvent extends EventCancellableBase {

    @Getter
    private final UdpServerConnection connection;

    @Getter
    @Setter
    private Packet<?> packet;

    public UdpPacketSendEvent(@NotNull UdpServerConnection connection, @NotNull Packet<?> packet) {
        this.connection = checkNotNull(connection, "connection cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
