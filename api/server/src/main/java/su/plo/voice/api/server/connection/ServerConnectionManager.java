package su.plo.voice.api.server.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;

import javax.annotation.Nullable;
import java.util.function.Predicate;

// todo: doc
public interface ServerConnectionManager<T extends PacketHandler> {

    void broadcast(@NotNull Packet<T> packet, @Nullable Predicate<VoicePlayer> filter);
}
