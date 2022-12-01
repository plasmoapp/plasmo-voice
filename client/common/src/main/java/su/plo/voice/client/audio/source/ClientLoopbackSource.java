package su.plo.voice.client.audio.source;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.audio.source.LoopbackSource;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.api.util.Params;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Optional;

@RequiredArgsConstructor
public final class ClientLoopbackSource implements LoopbackSource {

    private final MinecraftClientLib minecraft;
    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;
    private final boolean relative;

    private final float[] position = new float[3];
    private final Pos3d playerPosition = new Pos3d(0D, 0D, 0D);

    private SourceGroup sourceGroup;
    @Getter
    private boolean stereo;
    @Setter
    private DoubleConfigEntry volumeEntry;

    @Override
    public Optional<SourceGroup> getSourceGroup() {
        return Optional.ofNullable(sourceGroup);
    }

    @Override
    public void initialize(boolean stereo) throws DeviceException {
        this.stereo = stereo;
        this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup(DeviceType.OUTPUT);
        sourceGroup.create(stereo, Params.EMPTY);

        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                device.runInContext(() -> {
                    alSource.setFloat(0x100E, 4F); // AL_MAX_GAIN
                    if (relative) alSource.setInt(0x202, 1); // AL_SOURCE_RELATIVE

                    alSource.play();
                });
            }
        }
    }

    @Override
    public void close() {
        if (sourceGroup == null) return;
        sourceGroup.clear();
    }

    @Override
    public void write(short[] samples) {
        if (sourceGroup == null) return; // not initialized yet

        if (!relative) {
            minecraft.getClientPlayer().ifPresent((player) -> {
                player.getPosition(playerPosition);

                position[0] = (float) playerPosition.getX();
                position[1] = (float) (playerPosition.getY() + player.getEyeHeight());
                position[2] = (float) playerPosition.getZ();
            });
        }

        float volume = config.getVoice().getVolume().value().floatValue();
        if (volumeEntry != null) {
            volume *= volumeEntry.value().floatValue();
        }
        updateSources(volume);
        for (DeviceSource source : sourceGroup.getSources()) {
            samples = source.getDevice().processFilters(samples);
            source.write(AudioUtil.shortsToBytes(samples));
        }
    }

    private void updateSources(float volume) {
        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                AlAudioDevice device = (AlAudioDevice) alSource.getDevice();

                device.runInContext(() -> {
                    alSource.setVolume(volume);
                    if (!relative) alSource.setFloatArray(0x1004, position); // AL_POSITION
                });
            }
        }
    }
}
