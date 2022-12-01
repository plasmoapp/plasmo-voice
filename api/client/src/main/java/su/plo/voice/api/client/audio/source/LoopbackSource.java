package su.plo.voice.api.client.audio.source;

import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.SourceGroup;

import java.util.Optional;

public interface LoopbackSource {

    Optional<SourceGroup> getSourceGroup();

    void initialize(boolean stereo) throws DeviceException;

    void close();

    void write(short[] samples);

    boolean isStereo();

    void setVolumeEntry(DoubleConfigEntry entry);
}
