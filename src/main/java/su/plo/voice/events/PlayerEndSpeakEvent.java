package su.plo.voice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerEndSpeakEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final Player player;

    public PlayerEndSpeakEvent(Player player) {
        super(true);
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
