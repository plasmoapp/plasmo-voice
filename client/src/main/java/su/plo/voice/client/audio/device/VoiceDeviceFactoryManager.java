package su.plo.voice.client.audio.device;

import com.google.common.collect.Maps;
import su.plo.voice.api.client.audio.device.DeviceFactory;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class VoiceDeviceFactoryManager implements DeviceFactoryManager {
    protected final Map<String, DeviceFactory> deviceFactories = Maps.newConcurrentMap();

    @Override
    public void registerDeviceFactory(DeviceFactory factory) {
        if (deviceFactories.containsKey(factory.getName())) {
            throw new IllegalArgumentException("Device factory with the same type already exist");
        }

        deviceFactories.put(factory.getName(), factory);
    }

    @Override
    public boolean unregisterDeviceFactory(DeviceFactory factory) {
        return unregisterDeviceFactory(factory.getName());
    }

    @Override
    public boolean unregisterDeviceFactory(String type) {
        return deviceFactories.remove(type) != null;
    }

    @Override
    public Optional<DeviceFactory> getDeviceFactory(String type) {
        return Optional.ofNullable(deviceFactories.get(type));
    }

    @Override
    public Collection<DeviceFactory> getDeviceFactories() {
        return deviceFactories.values();
    }
}
