package su.plo.voice.api.server.event.player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.player.VoicePlayer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fires when a player's synchronized permission updates.
 */
public final class PlayerPermissionUpdateEvent implements Event {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final String permission;

    public PlayerPermissionUpdateEvent(@NotNull VoicePlayer player, @NotNull String permission) {
        this.player = checkNotNull(player, "player cannot be null");
        this.permission = checkNotNull(permission, "permission cannot be null");
    }
}
