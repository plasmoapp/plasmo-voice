package su.plo.voice.api.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.Collection;
import java.util.UUID;

public interface ClientPlayersSet {

    void addPlayer(@NotNull MinecraftGameProfile player);

    boolean removePlayer(@NotNull UUID playerId);

    void clearPlayers();

    Collection<MinecraftGameProfile> getPlayers();
}
