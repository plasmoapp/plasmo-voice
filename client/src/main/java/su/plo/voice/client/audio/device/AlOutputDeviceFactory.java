package su.plo.voice.client.audio.device;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public final class AlOutputDeviceFactory implements DeviceFactory {

    private final PlasmoVoiceClient voiceClient;

    @Override
    public @NotNull AudioDevice openDevice(@NotNull AudioFormat format, @Nullable String deviceName) throws DeviceException {
        checkNotNull(format, "format cannot be null");

        if (Strings.isNullOrEmpty(deviceName)) {
            deviceName = getDefaultDeviceName();
        }

        return new AlOutputDevice(voiceClient, deviceName, format);
    }

    @Override
    public @NotNull String getDefaultDeviceName() {
        return ALC11.alcGetString(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
    }

    @Override
    public @NotNull ImmutableList<String> getDeviceNames() {
        List<String> devices = ALUtil.getStringList(0L, ALC11.ALC_ALL_DEVICES_SPECIFIER);
        return devices == null ? ImmutableList.of() : ImmutableList.copyOf(devices);
    }

    @Override
    public @NotNull String getName() {
        return "AL_OUTPUT";
    }
}
