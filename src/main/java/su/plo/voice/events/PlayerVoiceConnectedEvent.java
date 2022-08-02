package su.plo.voice.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fires when player connects to voice chat
 */
@AllArgsConstructor
public class PlayerVoiceConnectedEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Getter
    private final Player player;

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
