package su.plo.voice.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class VoiceClientSourceLine extends VoiceSourceLine implements ClientSourceLine {

    private final DoubleConfigEntry volumeEntry;

    public VoiceClientSourceLine(@NotNull DoubleConfigEntry volumeEntry,
                                 @NotNull SourceLine line) {
        super(
                line.getName(),
                line.getTranslation(),
                line.getIcon(),
                line.getWeight(),
                line.hasPlayers() ? new CopyOnWriteArraySet<>(line.getPlayers()) : null
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
    public void addPlayer(@NotNull UUID playerId) {
        players.add(playerId);
    }

    @Override
    public boolean removePlayer(@NotNull UUID playerId) {
        return players.remove(playerId);
    }

    @Override
    public void clearPlayers() {
        players.clear();
    }
}
