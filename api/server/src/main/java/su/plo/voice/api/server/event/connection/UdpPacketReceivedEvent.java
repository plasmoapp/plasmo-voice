package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the {@link su.plo.voice.api.server.socket.UdpServer}
 * is received the packet, but not handled yet
 */
public class UdpPacketReceivedEvent extends EventCancellableBase {

    @Getter
    private final UdpConnection connection;

    @Getter
    private final Packet<?> packet;

    public UdpPacketReceivedEvent(@NotNull UdpConnection connection, @NotNull Packet<?> packet) {
        this.connection = checkNotNull(connection, "connection cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
