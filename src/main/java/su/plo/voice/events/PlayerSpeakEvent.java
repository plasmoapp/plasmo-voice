package su.plo.voice.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSpeakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final long sequenceNumber;
    private byte[] data;
    private short distance;

    public PlayerSpeakEvent(Player player, byte[] data, short distance, long sequenceNumber) {
        super(true);
        this.player = player;
        this.data = data;
        this.distance = distance;
        this.sequenceNumber = sequenceNumber;
    }

    public Player getPlayer() {
        return this.player;
    }

    public byte[] getData() {
        return data;
    }

    public short getDistance() {
        return distance;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDistance(short distance) {
        this.distance = distance;
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

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
