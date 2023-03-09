package su.plo.voice.client.audio.line;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.data.player.MinecraftGameProfile;

import java.util.UUID;

public class VoiceClientSourceLine extends VoiceSourceLine implements ClientSourceLine {

    private final DoubleConfigEntry volumeEntry;

    public VoiceClientSourceLine(@NotNull DoubleConfigEntry volumeEntry,
                                 @NotNull SourceLine line) {
        super(
                line.getName(),
                line.getTranslation(),
                line.getIcon(),
                line.getDefaultVolume(),
                line.getWeight(),
                line.hasPlayers() ? Sets.newConcurrentHashSet(line.getPlayers()) : null
        );

        this.volumeEntry = volumeEntry;
    }

    @Override
    public void setVolume(double volume) {
        volumeEntry.set(volume);
    }

    @Override
    public double getVolume() {
        return volumeEntry.getMax();
    }

    public DoubleConfigEntry getVolumeConfigEntry() {
        return volumeEntry;
    }

    @Override
    public void addPlayer(@NotNull MinecraftGameProfile playerId) {
        if (players == null) {
            players = Sets.newConcurrentHashSet();
        }

        players.add(playerId);
    }

    @Override
    public boolean removePlayer(@NotNull UUID playerId) {
        return players.removeIf((player) -> player.getId().equals(playerId));
    }

    @Override
    public void clearPlayers() {
        players.clear();
    }
}
