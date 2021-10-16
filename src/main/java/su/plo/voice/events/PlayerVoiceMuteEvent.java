package su.plo.voice.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerVoiceMuteEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private final OfflinePlayer player;
    private final Long to;

    public PlayerVoiceMuteEvent(OfflinePlayer player, long duration) {
        this.player = player;
        this.to = duration;
    }

    public Long getTo() {
        return to;
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
