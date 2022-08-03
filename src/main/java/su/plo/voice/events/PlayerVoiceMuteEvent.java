package su.plo.voice.events;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fires when the player has been muted
 */
public class PlayerVoiceMuteEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private final OfflinePlayer player;
    private final Long muteDuration;

    public PlayerVoiceMuteEvent(OfflinePlayer player, long duration) {
        this.player = player;
        this.muteDuration = duration;
    }

    /**
     * @return <= 0 if mute is permanent, else return usual timestamp in milliseconds
     */
    public Long getMuteDuration() {
        return muteDuration;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
