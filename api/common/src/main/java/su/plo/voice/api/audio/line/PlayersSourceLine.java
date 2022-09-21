package su.plo.voice.api.audio.line;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayersSourceLine {

    void addPlayer(@NotNull UUID playerId);

    boolean removePlayer(@NotNull UUID playerId);

    void clearPlayers();
}
