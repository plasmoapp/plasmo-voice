package su.plo.voice.api.audio.line;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface PlayerMap {

    void addPlayer(@NotNull UUID playerId);

    boolean removePlayer(@NotNull UUID playerId);

    void clearPlayers();

    Collection<UUID> getPlayers();
}
