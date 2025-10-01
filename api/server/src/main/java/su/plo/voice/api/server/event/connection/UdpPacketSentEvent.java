package su.plo.voice.api.server.event.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.packets.Packet;

/**
 * This event is fired when a packet is sent to the player's {@link UdpServerConnection}.
 */
@AllArgsConstructor
@Getter
public final class UdpPacketSentEvent implements Event {

    private final @NonNull UdpServerConnection connection;
    private final @NonNull Packet<?> packet;
}
