package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

// todo: doc
public interface ConnectionManager<T extends PacketHandler, P extends VoicePlayer<?>> {

    /**
     * Broadcasts packet to all players with voice chat installed
     */
    void broadcast(@NotNull Packet<T> packet, @Nullable Predicate<P> filter);

    /**
     * Broadcasts packet to all players with voice chat installed
     */
    default void broadcast(@NotNull Packet<T> packet) {
        broadcast(packet, null);
    }
}
