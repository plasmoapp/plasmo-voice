package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.player.VoicePlayer;

public interface ServerPlayersSourceLine extends ServerSourceLine {

    /**
     * Set your own implementation of {@link ServerPlayerMap} for this player
     *
     * @param player the player
     * @param playerMap the player map
     */
    void setPlayerMap(@NotNull VoicePlayer player, @NotNull ServerPlayerMap playerMap);

    @NotNull ServerPlayerMap getPlayerMap(@NotNull VoicePlayer player);
}
