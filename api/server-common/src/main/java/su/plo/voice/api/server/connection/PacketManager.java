package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Represents a base interface for sending a Plasmo Voice related packets to the players.
 */
public interface PacketManager<T extends PacketHandler, P extends VoicePlayer> {

    /**
     * Broadcasts a packet to all players with voice chat installed.
     *
     * @param packet The packet to broadcast.
     * @param filter A predicate to filter the target players. Return {@code true} if you want to include a player.
     */
    void broadcast(@NotNull Packet<T> packet, @Nullable Predicate<P> filter);

    /**
     * Broadcasts a packet to all players with voice chat installed.
     *
     * @param packet The packet to broadcast.
     */
    default void broadcast(@NotNull Packet<T> packet) {
        broadcast(packet, null);
    }
}
