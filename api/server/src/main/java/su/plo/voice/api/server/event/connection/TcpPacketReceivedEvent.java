package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.proto.packets.Packet;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the PlayerChannelHandler
 * is received the packet, but not handled yet
 */
public final class TcpPacketReceivedEvent extends EventCancellableBase {

    @Getter
    private final VoiceServerPlayer player;

    @Getter
    private final Packet<?> packet;

    public TcpPacketReceivedEvent(@NotNull VoiceServerPlayer player, @NotNull Packet<?> packet) {
        this.player = checkNotNull(player, "player cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
