package su.plo.voice.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.ClientSourceLine;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;

public class VoiceClientSourceLine extends VoiceSourceLine implements ClientSourceLine {

    private final DoubleConfigEntry volumeEntry;

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
}
