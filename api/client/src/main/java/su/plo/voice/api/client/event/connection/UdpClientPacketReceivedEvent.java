package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the {@link UdpClient}
 * is received the packet, but not handled yet
 */
public final class UdpClientPacketReceivedEvent extends EventCancellableBase {

    @Getter
    private final UdpClient client;

    @Getter
    private final Packet<?> packet;

    public UdpClientPacketReceivedEvent(@NotNull UdpClient client, @NotNull Packet<?> packet) {
        this.client = checkNotNull(client, "client cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
