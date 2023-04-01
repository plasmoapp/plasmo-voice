package su.plo.voice.client.audio.device;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.device.AudioDevice;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.DeviceFactory;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JavaxInputDeviceFactory implements DeviceFactory {

    private final PlasmoVoiceClient client;

    public JavaxInputDeviceFactory(PlasmoVoiceClient client) {
        this.client = client;
    }

    @Override
    public AudioDevice openDevice(@NotNull AudioFormat format, @Nullable String deviceName) throws DeviceException {
        checkNotNull(format, "format cannot be null");

        if (Strings.emptyToNull(deviceName) == null) {
            deviceName = getDefaultDeviceName();
        }

        return new JavaxInputDevice(client, deviceName, format);
    }

    @Override
    public String getDefaultDeviceName() {
        return getDeviceNames().iterator().next();
    }

    @Override
    public ImmutableList<String> getDeviceNames() {
        List<String> devices = new ArrayList<>();
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info lineInfo = new Line.Info(TargetDataLine.class);

            if (mixer.isLineSupported(lineInfo)) {
                devices.add(mixerInfo.getName());
            }
        }

        return ImmutableList.copyOf(devices);
    }

    @Override
    public String getType() {
        return "JAVAX_INPUT";
    }
}
