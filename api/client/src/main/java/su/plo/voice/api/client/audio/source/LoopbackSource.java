package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.api.client.audio.device.source.AlSource;

import java.util.Optional;

/**
 * Represents a client-only direct source.
 */
public interface LoopbackSource {

    /**
     * Initializes the loopback source.
     *
     * @param stereo {@code true} if the source should be initialized as stereo, {@code false} for mono.
     * @throws DeviceException If there is an issue initializing the loopback source.
     */
    void initialize(boolean stereo) throws DeviceException;

    /**
     * Closes the loopback source, releasing any associated resources.
     */
    void close();

    /**
     * Checks if the audio source is closed or not initialized.
     *
     * @return Whether the source is closed.
     */
    boolean isClosed();

    /**
     * Writes audio samples to the loopback source.
     *
     * @param samples An array of audio samples to write.
     */
    void write(short[] samples);

    /**
     * Checks if the loopback source is initialized in stereo.
     *
     * @return {@code true} if the source is stereo, {@code false} for mono.
     */
    boolean isStereo();

    /**
     * Configures the volume control for the loopback source.
     *
     * @param entry A configuration entry that represents the volume control. Set to null to remove the volume control.
     */
    void setVolumeEntry(@Nullable DoubleConfigEntry entry);

    /**
     * Gets the source group associated with this loopback source.
     *
     * @return An optional containing the associated source group, if available.
     */
    @NotNull Optional<AlSource> getSource();
}
