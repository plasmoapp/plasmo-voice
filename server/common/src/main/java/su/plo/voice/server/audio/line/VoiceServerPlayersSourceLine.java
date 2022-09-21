package su.plo.voice.server.audio.line;

import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerPlayersSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@ToString(callSuper = true)
public final class VoiceServerPlayersSourceLine extends VoiceServerSourceLine implements ServerPlayersSourceLine {

    private final Set<UUID> players = new HashSet<>();

    public VoiceServerPlayersSourceLine(@NotNull PlasmoVoiceServer voiceServer,
                                        @NotNull AddonContainer addon,
                                        @NotNull String name,
                                        @NotNull String translation,
                                        @NotNull String icon,
                                        int weight) {
        super(voiceServer, addon, name, translation, icon, weight);
    }

    @Override
    public void addPlayer(@NotNull VoicePlayer player) {
        players.add(player.getUUID());
        // todo: send update
    }

    @Override
    public boolean removePlayer(@NotNull VoicePlayer player) {
        return removePlayer(player.getUUID());
    }

    @Override
    public boolean removePlayer(@NotNull UUID playerId) {
        // todo: send update
        return players.remove(playerId);
    }
}
