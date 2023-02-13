package su.plo.voice.client.audio.device.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.*;
import su.plo.voice.api.client.audio.device.source.DeviceSource;
import su.plo.voice.api.client.audio.device.source.SourceGroup;
import su.plo.voice.api.util.Params;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VoiceOutputSourceGroup implements SourceGroup {

    private final DeviceManager devices;

    private final List<DeviceSource> sources = new CopyOnWriteArrayList<>();

    public VoiceOutputSourceGroup(@NotNull DeviceManager devices) {
        this.devices = devices;
    }

    @Override
    public void create(boolean stereo, @NotNull Params params) throws DeviceException {
        for (AudioDevice device : devices.getDevices(DeviceType.OUTPUT)) {
            OutputDevice<?> outputDevice = (OutputDevice<?>) device;
            sources.add(outputDevice.createSource(stereo, params));
        }
    }

    @Override
    public void clear() {
        sources.forEach(DeviceSource::close);
        sources.clear();
    }

    @Override
    public Collection<DeviceSource> getSources() {
        return sources;
    }
}
