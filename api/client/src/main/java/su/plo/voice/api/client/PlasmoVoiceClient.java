package su.plo.voice.api.client;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;

/**
 * The Plasmo Client Voice API
 */
public interface PlasmoVoiceClient extends PlasmoVoice {

    /**
     * Gets the {@link DeviceFactoryManager}
     *
     * Device factories are used to create new devices
     *
     * @return the device factory manager
     */
    @NotNull DeviceFactoryManager getDeviceFactoryManager();

    /**
     * Gets the {@link DeviceManager}
     *
     * This manager can be used to set primary devices
     *
     * @return the device manager
     */
    @NotNull DeviceManager getDeviceManager();
}
