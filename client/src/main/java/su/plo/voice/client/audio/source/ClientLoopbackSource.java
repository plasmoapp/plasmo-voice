package su.plo.voice.client.audio.source;

import gg.essential.universal.UMinecraft;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AlContextAudioDevice;
import su.plo.voice.api.client.audio.device.AlContextOutputDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;
import su.plo.voice.api.client.audio.device.source.AlSourceParams;
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

    private @Nullable AlSource source;
    @Getter
    private boolean stereo;
    @Setter
    private DoubleConfigEntry volumeEntry;

    @Override
    public @NotNull Optional<AlSource> getSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public void initialize(boolean stereo) throws DeviceException {
        AlContextOutputDevice device = voiceClient.getDeviceManager().getOutputDevice()
                .orElseThrow(() -> new DeviceException("Output device is null"));

        this.stereo = stereo;
        this.source = device.createSource(stereo, AlSourceParams.DEFAULT);

        source.setCloseTimeoutMs(0L);

        device.runInContextBlocking(() -> {
            source.setFloat(0x100E, 4F); // AL_MAX_GAIN
            if (relative) source.setInt(0x202, 1); // AL_SOURCE_RELATIVE

            source.play();
        });
    }

    @Override
    public void close() {
        if (source == null) return;
        source.closeAsync();
    }

    @Override
    public void write(short[] samples) {
        if (source == null) return; // not initialized yet

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

        samples = source.getDevice().processFilters(samples);
        source.write(AudioUtil.shortsToBytes(samples));
    }

    private void updateSources(float volume) {
        AlContextAudioDevice device = source.getDevice();

        device.runInContextBlocking(() -> {
            source.setVolume(volume);
            if (!relative) source.setFloatArray(0x1004, position); // AL_POSITION
        });
    }
}
