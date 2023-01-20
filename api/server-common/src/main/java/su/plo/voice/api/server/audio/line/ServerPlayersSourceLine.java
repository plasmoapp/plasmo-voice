package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.PlayerMap;
import su.plo.voice.api.server.player.VoicePlayer;

public interface ServerPlayersSourceLine extends ServerSourceLine {

    /**
     * Set your own implementation of {@link PlayerMap} for this player
     *
     * @param player the player
     * @param playerMap the player map
     */
    void setPlayerMap(@NotNull VoicePlayer<?> player, @NotNull PlayerMap playerMap);

    @NotNull PlayerMap getPlayerMap(@NotNull VoicePlayer<?> player);
}
