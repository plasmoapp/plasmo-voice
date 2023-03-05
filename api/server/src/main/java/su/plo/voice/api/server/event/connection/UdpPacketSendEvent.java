package su.plo.voice.api.server.event.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;

/**
 * This event is fired when the {@link su.plo.voice.api.server.socket.UdpServer}
 * is about to send packet to {@link UdpServerConnection}
 * <br/>
 * Packet can be replaced by {@link #setPacket(Packet)}
 */
@AllArgsConstructor
public final class UdpPacketSendEvent extends EventCancellableBase {

    @Getter
    private final @NonNull UdpServerConnection connection;
    @Getter
    @Setter
    private @NonNull Packet<?> packet;
}
