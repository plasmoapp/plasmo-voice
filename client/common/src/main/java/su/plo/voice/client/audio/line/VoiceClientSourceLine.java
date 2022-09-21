package su.plo.voice.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class VoiceClientSourceLine extends VoiceSourceLine implements ClientSourceLine {

    private final DoubleConfigEntry volumeEntry;
    private final Set<UUID> players = new CopyOnWriteArraySet<>();

    public VoiceClientSourceLine(@NotNull DoubleConfigEntry volumeEntry,
                                 @NotNull SourceLine line) {
        super(
                line.getName(),
                line.getTranslation(),
                line.getIcon(),
                line.getWeight()
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
