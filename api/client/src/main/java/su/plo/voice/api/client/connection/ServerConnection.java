package su.plo.voice.api.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ServerConnection {

    Collection<VoicePlayerInfo> getPlayers();

    Optional<VoicePlayerInfo> getPlayerById(@NotNull UUID playerId);

    Optional<VoicePlayerInfo> getClientPlayer();

    void sendPacket(Packet<?> packet);

    void close();
}
