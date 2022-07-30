package su.plo.voice.api.audio.device;

import java.util.Collection;
import java.util.Optional;

// todo: doc
public interface DeviceFactoryManager {

    /**
     * Registers the device factory
     *
     * Device factories are used to create new devices
     *
     * @param factory the device factory
     */
    void registerDeviceFactory(DeviceFactory factory);

    boolean unregisterDeviceFactory(DeviceFactory factory);

    boolean unregisterDeviceFactory(String type);

    Optional<DeviceFactory> getDeviceFactory(String type);

    Collection<DeviceFactory> getDeviceFactories();
}
