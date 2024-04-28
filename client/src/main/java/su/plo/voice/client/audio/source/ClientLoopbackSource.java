package su.plo.voice.client.audio.source;

import su.plo.voice.api.client.audio.device.source.AlSourceParams;
import gg.essential.universal.UMinecraft;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlContextAudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.client.audio.source.LoopbackSource;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.VoiceClientConfig;

import java.util.Optional;

@RequiredArgsConstructor
public final class ClientLoopbackSource implements LoopbackSource {

    private final PlasmoVoiceClient voiceClient;
    private final VoiceClientConfig config;
    private final boolean relative;

    private final float[] position = new float[3];

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
        this.sourceGroup = voiceClient.getDeviceManager().createSourceGroup();
        sourceGroup.create(stereo, AlSourceParams.DEFAULT);

        for (DeviceSource source : sourceGroup.getSources()) {
            if (source instanceof AlSource) {
                AlSource alSource = (AlSource) source;
                alSource.setCloseTimeoutMs(0L);
                AlContextAudioDevice device = alSource.getDevice();

                device.runInContextBlocking(() -> {
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
            LocalPlayer player = UMinecraft.getPlayer();
            if (player == null) return;

            position[0] = (float) player.getX();
            position[1] = (float) (player.getY() + player.getEyeHeight());
            position[2] = (float) player.getZ();
        }

        float volume = config.getVoice().getVolume().value().floatValue();
        if (volumeEntry != null) {
            volume *= volumeEntry.value().floatValue();
        }

        if (config.getAdvanced().getExponentialVolumeSlider().value()) {
            volume = (float) Math.pow(volume, 3);
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
                AlContextAudioDevice device = alSource.getDevice();

                device.runInContextBlocking(() -> {
                    alSource.setVolume(volume);
                    if (!relative) alSource.setFloatArray(0x1004, position); // AL_POSITION
                });
            }
        }
    }
}
