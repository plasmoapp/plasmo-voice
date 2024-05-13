package su.plo.voice.api.client;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonsLoader;
import su.plo.voice.api.addon.ClientAddonsLoader;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.config.ClientConfig;
import su.plo.voice.api.client.config.addon.AddonConfig;
import su.plo.voice.api.client.config.hotkey.Hotkeys;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.client.time.SystemTimeSupplier;
import su.plo.voice.api.client.time.TimeSupplier;

import java.util.Optional;

/**
 * The Plasmo Voice Client API.
 */
public interface PlasmoVoiceClient extends PlasmoVoice {

    /**
     * Gets the client's addons loaders.
     *
     * <p>
     *     Use this method to get the addons loader for loading client addons.
     * </p>
     *
     * @return The {@link AddonsLoader}.
     */
    static AddonsLoader getAddonsLoader() {
        return ClientAddonsLoader.INSTANCE;
    }

    /**
     * Gets the {@link DeviceFactoryManager}.
     *
     * <p>
     *     Device factories are used to create new devices.
     * </p>
     *
     * @return The {@link DeviceFactoryManager}.
     */
    @NotNull DeviceFactoryManager getDeviceFactoryManager();

    /**
     * Gets the {@link DeviceManager}.
     *
     * <p>
     *     This manager can be used to set primary devices.
     * </p>
     *
     * @return The {@link DeviceManager}.
     */
    @NotNull DeviceManager getDeviceManager();

    /**
     * Gets the {@link AudioCapture}.
     *
     * @return The {@link AudioCapture}.
     */
    @NotNull AudioCapture getAudioCapture();

    /**
     * Gets the {@link ClientActivationManager}.
     *
     * @return The {@link ClientActivationManager}.
     */
    @NotNull ClientActivationManager getActivationManager();

    /**
     * Gets the {@link ClientSourceLineManager}.
     *
     * @return The {@link ClientSourceLineManager}.
     */
    @NotNull ClientSourceLineManager getSourceLineManager();

    /**
     * Gets the {@link UdpClientManager}.
     *
     * <p>
     *     This manager is used to manage the current {@link UdpClient}.
     * </p>
     *
     * @return The {@link UdpClientManager}.
     */
    @NotNull UdpClientManager getUdpClientManager();

    /**
     * Gets the current {@link ServerInfo}.
     *
     * @return An optional {@link ServerInfo}.
     */
    Optional<ServerInfo> getServerInfo();

    /**
     * Gets the current {@link ServerConnection}.
     *
     * @return An optional {@link ServerConnection}.
     */
    Optional<ServerConnection> getServerConnection();

    /**
     * Gets the {@link Hotkeys}.
     *
     * @return The {@link Hotkeys}.
     */
    @NotNull Hotkeys getHotkeys();

    /**
     * Gets the {@link DistanceVisualizer}.
     *
     * @return The {@link DistanceVisualizer}.
     */
    @NotNull DistanceVisualizer getDistanceVisualizer();

    /**
     * Gets the {@link ClientSourceManager}.
     *
     * @return The {@link ClientSourceManager}.
     */
    @NotNull ClientSourceManager getSourceManager();

    /**
     * Gets the {@link AddonConfig} for the specified addon.
     *
     * @param addon The addon.
     *
     * @return The {@link AddonConfig}.
     */
    @NotNull AddonConfig getAddonConfig(@NotNull Object addon);

    /**
     * Gets the {@link ClientConfig}.
     *
     * @return The {@link ClientConfig}.
     */
    @NotNull ClientConfig getConfig();

    /**
     * Sets the time supplier.
     * <br/>
     * Default is {@link SystemTimeSupplier}.
     */
    void setTimeSupplier(@NotNull TimeSupplier timeSupplier);

    /**
     * Gets the current time supplier.
     * <br/>
     * Time supplier is used in sources,
     * so you can change the time supplier to prevent closing the sources in environments with custom time
     * (for example, in replays).
     * <br/>
     * Default is {@link SystemTimeSupplier}.
     *
     * @return The current time supplier.
     */
    @NotNull TimeSupplier getTimeSupplier();
}
