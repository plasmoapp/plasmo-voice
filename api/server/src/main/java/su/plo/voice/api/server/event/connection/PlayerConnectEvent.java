package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellable;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the server is about to send {@link ConnectionPacket} to the player
 */
public final class PlayerConnectEvent extends PlayerEvent implements EventCancellable {

    @Getter
    @Setter
    private ConnectionPacket packet;

    private boolean cancel;

    public PlayerConnectEvent(@NotNull VoicePlayer player, @NotNull ConnectionPacket packet) {
        super(player);

        this.packet = checkNotNull(packet, "packet cannot be null");
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
}
