package su.plo.voice.client.audio.device;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.source.SourceGroup;
import su.plo.voice.client.audio.source.VoiceSourceGroup;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceDeviceManager implements DeviceManager {

    private final List<AudioDevice> inputDevices = new CopyOnWriteArrayList<>();
    private final List<AudioDevice> outputDevices = new CopyOnWriteArrayList<>();

    @Override
    public void add(@NotNull AudioDevice device) {
        checkNotNull(device, "device cannot be null");

        List<AudioDevice> devices = getDevicesList(device);

        if (devices == inputDevices && devices.size() > 0) {
            throw new IllegalStateException("Multiple input devices currently are not supported. Use DeviceManager::replace to replace the current input device");
        }

        if (devices.contains(device)) return;
        devices.add(device);
    }

    @Override
    public void replace(@NotNull AudioDevice oldDevice, @NotNull AudioDevice newDevice) {
        checkNotNull(oldDevice, "oldDevice cannot be null");
        checkNotNull(oldDevice, "newDevice cannot be null");

        List<AudioDevice> devices = getDevicesList(newDevice);

        if (devices != getDevicesList(oldDevice)) {
            throw new IllegalArgumentException("Devices are not implementing the same interface");
        }

        int index = devices.indexOf(oldDevice);
        if (index < 0) {
            throw new IllegalArgumentException("oldDevice not found in device list");
        }

        devices.set(index, newDevice);
    }

    @Override
    public void remove(@NotNull AudioDevice device) {
        checkNotNull(device, "device cannot be null");
        getDevicesList(device).remove(device);
    }

    @Override
    public void clear(@Nullable DeviceType type) {
        if (type == DeviceType.INPUT) {
            inputDevices.clear();
        } else {
            outputDevices.clear();
        }
    }

    @Override
    public Collection<AudioDevice> getDevices(DeviceType type) {
        if (type == DeviceType.INPUT) {
            return ImmutableList.copyOf(inputDevices);
        } else if (type == DeviceType.OUTPUT) {
            return ImmutableList.copyOf(outputDevices);
        } else {
            ImmutableList.Builder<AudioDevice> builder = ImmutableList.builder();
            return builder.addAll(inputDevices).addAll(outputDevices).build();
        }
    }

    @Override
    public SourceGroup createSourceGroup(@Nullable DeviceType type) {
        return new VoiceSourceGroup();
    }

    private List<AudioDevice> getDevicesList(AudioDevice device) {
        List<AudioDevice> devices;
        if (device instanceof InputDevice) {
            devices = inputDevices;
        } else if (device instanceof OutputDevice) {
            devices = outputDevices;
        } else {
            throw new IllegalArgumentException("device not implements InputDevice or OutputDevice");
        }

        return devices;
    }
}
