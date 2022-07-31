package su.plo.voice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import su.plo.voice.common.packets.Packet;

public class PlayerSpeakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final Packet packet;

    public PlayerSpeakEvent(Player player, Packet packet) {
        super(true);
        this.player = player;
        this.packet = packet;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public Packet getPacket() {
        return packet;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
