package su.plo.voice.client.audio.device;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import javax.sound.sampled.AudioFormat;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AlInputDeviceFactory implements DeviceFactory {

    private final PlasmoVoiceClient client;

    public AlInputDeviceFactory(PlasmoVoiceClient client) {
        this.client = client;
    }

    @Override
    public @NotNull AudioDevice openDevice(@NotNull AudioFormat format, @Nullable String deviceName) throws DeviceException {
        checkNotNull(format, "format cannot be null");

        return new AlInputDevice(client, deviceName, format);
    }

    @Override
    public @NotNull String getDefaultDeviceName() {
        return ALC11.alcGetString(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
    }

    @Override
    public @NotNull ImmutableList<String> getDeviceNames() {
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_CAPTURE_DEVICE_SPECIFIER);
        return devices == null ? ImmutableList.of() : ImmutableList.copyOf(devices);
    }

    @Override
    public @NotNull String getName() {
        return "AL_INPUT";
    }
}
