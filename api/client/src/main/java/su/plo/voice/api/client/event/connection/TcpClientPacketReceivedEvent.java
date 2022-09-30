package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the {@link ServerConnection}
 * is received the packet, but not handled yet
 */
public final class TcpClientPacketReceivedEvent extends EventCancellableBase {

    @Getter
    private final ServerConnection connection;
    @Getter
    private final Packet<?> packet;

    public TcpClientPacketReceivedEvent(@NotNull ServerConnection connection, @NotNull Packet<?> packet) {
        this.connection = checkNotNull(connection, "connection cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
