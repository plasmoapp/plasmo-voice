package su.plo.voice.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.common.packets.tcp.ConfigPacket;

public class PlayerConfigEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS_LIST = new HandlerList();
    @Getter
    private final Player player;
    @Getter
    private final ConfigPacket config;
    @Getter
    private final Cause cause;
    @Getter
    private boolean cancelled = false;

    public PlayerConfigEvent(Player player, ConfigPacket config, Cause cause) {
        super(true);
        this.player = player;
        this.config = config;
        this.cause = cause;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public void setCancelled(boolean cancel) {
        if (cancel && cause.equals(Cause.CONNECT)) {
            throw new IllegalStateException("Cannot cancelPlayerConfigEvent event with cause CONNECT");
        }

        this.cancelled = cancel;
    }

    public enum Cause {
        CONNECT,
        RELOAD,
        PLUGIN // called from API
    }
}
