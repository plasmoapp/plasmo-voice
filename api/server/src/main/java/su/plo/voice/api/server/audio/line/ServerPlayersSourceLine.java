package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.UUID;

public interface ServerPlayersSourceLine extends ServerSourceLine {

    void addPlayer(@NotNull VoicePlayer player);

    boolean removePlayer(@NotNull VoicePlayer player);

    boolean removePlayer(@NotNull UUID playerId);
}
