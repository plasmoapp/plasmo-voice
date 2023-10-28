package su.plo.voice.api.client.audio.device;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Manages device factories.
 * <br/>
 * Device factories are used to create new audio devices.
 */
public interface DeviceFactoryManager {

    /**
     * Registers a device factory.
     *
     * @param factory The device factory to register.
     */
    void registerDeviceFactory(DeviceFactory factory);

    /**
     * Unregisters a device factory.
     *
     * @param factory The device factory to unregister.
     * @return {@code true} if the factory was removed from the map, {@code false} otherwise.
     */
    boolean unregisterDeviceFactory(DeviceFactory factory);

    /**
     * Unregisters a device factory by its unique type identifier.
     *
     * @param type The unique type identifier of the factory to unregister.
     * @return {@code true} if the factory was removed from the map, {@code false} otherwise.
     */
    boolean unregisterDeviceFactory(String type);

    /**
     * Gets a device factory by its unique type identifier.
     *
     * @param type The unique type identifier of the factory to retrieve.
     * @return An optional containing the device factory if found, or an empty optional if not found.
     */
    Optional<DeviceFactory> getDeviceFactory(String type);

    /**
     * Gets a collection of all registered device factories.
     *
     * @return A collection of registered device factories.
     */
    @NotNull Collection<DeviceFactory> getDeviceFactories();
}
