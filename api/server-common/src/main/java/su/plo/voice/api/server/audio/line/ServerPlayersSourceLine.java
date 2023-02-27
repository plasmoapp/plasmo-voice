package su.plo.voice.api.server.audio.line;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.player.VoicePlayer;

public interface ServerPlayersSourceLine extends ServerSourceLine {

    /**
     * Set your own implementation of {@link ServerPlayersSet} for this player
     *
     * @param player the player
     * @param playerMap the player map
     */
    void setPlayersSet(@NotNull VoicePlayer player, @Nullable ServerPlayersSet playerMap);

    @NotNull ServerPlayersSet getPlayersSet(@NotNull VoicePlayer player);

    /**
     * Creates a map that will automatically broadcast about the changes to all players in the map
     *
     * @return the player map
     */
    @NotNull ServerPlayersSet createBroadcastSet();
}
