package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.player.VoicePlayer;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class PlayerEvent implements Event {

    @Getter
    private final VoicePlayer player;

    public PlayerEvent(@NotNull VoicePlayer player) {
        this.player = checkNotNull(player, "player cannot be null");
    }
}
