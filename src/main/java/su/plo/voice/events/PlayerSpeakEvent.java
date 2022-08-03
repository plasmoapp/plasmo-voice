package su.plo.voice.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import su.plo.voice.common.packets.udp.VoiceServerPacket;

/**
 * Fires when player is speaking in voice chat
 */
public class PlayerSpeakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled = false;
    @Getter
    private final Player player;
    @Getter
    private final VoiceServerPacket packet;

    public PlayerSpeakEvent(Player player, VoiceServerPacket packet) {
        super(true);
        this.player = player;
        this.packet = packet;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
