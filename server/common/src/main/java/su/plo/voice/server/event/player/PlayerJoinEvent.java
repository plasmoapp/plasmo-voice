package su.plo.voice.server.event.player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fires when the player join the server
 */
public class PlayerJoinEvent implements Event {

    @Getter
    private final Object player;
    @Getter
    private final UUID playerId;

    public PlayerJoinEvent(@NotNull Object player, @NotNull UUID playerId) {
        this.player = checkNotNull(player, "player cannot be null");
        this.playerId = checkNotNull(playerId, "playerId cannot be null");
    }
}
