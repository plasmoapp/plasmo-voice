package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;

import java.util.UUID;

/**
 * This event is fired once the {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket} is received
 */
public final class VoicePlayerDisconnectedEvent implements Event {

    @Getter
    private final UUID playerId;

    public VoicePlayerDisconnectedEvent(@NotNull UUID playerId) {
        this.playerId = playerId;
    }
}
