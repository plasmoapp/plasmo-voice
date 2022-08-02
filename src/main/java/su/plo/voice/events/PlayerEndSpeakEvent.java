package su.plo.voice.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when the player's talk is ended
 */
public class PlayerEndSpeakEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private final Player player;

    public PlayerEndSpeakEvent(Player player) {
        super(true);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
