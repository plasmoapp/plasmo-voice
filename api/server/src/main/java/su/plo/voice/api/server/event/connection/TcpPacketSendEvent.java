package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.Packet;

/**
 * This event is fired when the server is about to send a {@link Packet} to the player.
 */
@RequiredArgsConstructor
public final class TcpPacketSendEvent extends EventCancellableBase {

    @Getter
    private final @NonNull VoiceServerPlayer player;
    @Getter
    private final @NonNull Packet<?> packet;
}
