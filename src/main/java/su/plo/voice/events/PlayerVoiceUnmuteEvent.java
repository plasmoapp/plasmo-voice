package su.plo.voice.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerVoiceUnmuteEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final OfflinePlayer player;

    public PlayerVoiceUnmuteEvent(OfflinePlayer player) {
        this.player = player;
    }

    public OfflinePlayer getPlayer() {
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
