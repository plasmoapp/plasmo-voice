package su.plo.voice.api.client;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;

import java.util.Optional;

/**
 * The Plasmo Client Voice API
 */
public interface PlasmoVoiceClient extends PlasmoVoice {

    /**
     * Gets the {@link DeviceFactoryManager}
     *
     * Device factories are used to create new devices
     *
     * @return {@link DeviceFactoryManager}
     */
    @NotNull DeviceFactoryManager getDeviceFactoryManager();

    /**
     * Gets the {@link DeviceManager}
     *
     * This manager can be used to set primary devices
     *
     * @return {@link DeviceManager}
     */
    @NotNull DeviceManager getDeviceManager();

    /**
     * Gets the {@link AudioCapture}
     *
     * @return {@link AudioCapture}
     */
    @NotNull AudioCapture getAudioCapture();

    /**
     * Gets the {@link UdpClientManager}
     *
     * This manager used to manage current {@link su.plo.voice.api.client.socket.UdpClient}
     *
     * @return {@link UdpClientManager}
     */
    @NotNull UdpClientManager getUdpClientManager();

    /**
     * Gets the current {@link ServerInfo}
     *
     * @return {@link ServerInfo}
     */
    Optional<ServerInfo> getServerInfo();

    /**
     * Gets the current {@link ServerConnection}
     *
     * @return {@link ServerConnection}
     */
    Optional<ServerConnection> getServerConnection();

    /**
     * Gets the {@link KeyBindings}
     *
     * @return {@link KeyBindings}
     */
    @NotNull KeyBindings getKeyBindings();
}
