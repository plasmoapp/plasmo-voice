package su.plo.voice.api.server.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.api.server.player.VoiceServerPlayer;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class PlayerEvent implements Event {

    @Getter
    private final VoiceServerPlayer player;

    public PlayerEvent(@NotNull VoiceServerPlayer player) {
        this.player = checkNotNull(player, "player cannot be null");
    }
}
