package su.plo.voice.api.client.audio.device;


import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// todo: doc
public interface AudioDevice {

    /**
     * Opens the device
     *
     * @param format the audio format
     * @param params device params
     *
     * @throws DeviceException if the device cannot be open
     */
    CompletableFuture<AudioDevice> open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException;

    /**
     * Closes the device
     */
    CompletableFuture<AudioDevice> close();

    /**
     * @return true if the device is open
     */
    boolean isOpen();

    /**
     * Adds the filter to device with priority
     */
    void addFilter(AudioFilter filter, AudioFilter.Priority priority);

    /**
     * Adds the filter to device with normal priority
     */
    void addFilter(AudioFilter filter);

    /**
     * Removes the filter from device
     */
    void removeFilter(AudioFilter filter);

    /**
     * Gets the device filters
     *
     * @return the device filters
     */
    Collection<AudioFilter> getFilters();

    /**
     * Gets the device name
     *
     * @return the device name
     */
    String getName();

    /**
     * Gets the device audio format
     *
     * @return the audio format with which audio device was open
     */
    Optional<AudioFormat> getFormat();

    /**
     * Gets the device buffer size calculated from format
     *
     * @return the buffer size
     */
    int getBufferSize();

    /**
     * Gets the device type
     *
     * @return the device type
     */
    DeviceType getType();
}
