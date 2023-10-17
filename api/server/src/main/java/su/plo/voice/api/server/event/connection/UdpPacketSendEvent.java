package su.plo.voice.api.server.event.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;

/**
 * This event is fired when the {@link UdpServer} is about to send a packet to the player's {@link UdpServerConnection}.
 */
@AllArgsConstructor
public final class UdpPacketSendEvent extends EventCancellableBase {

    @Getter
    private final @NonNull UdpServerConnection connection;
    @Getter
    private final @NonNull Packet<?> packet;
}
