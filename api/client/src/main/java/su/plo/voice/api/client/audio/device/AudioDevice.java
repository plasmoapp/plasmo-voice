package su.plo.voice.api.client.audio.device;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.audio.filter.AudioFilter;
import su.plo.voice.api.util.Params;

import javax.sound.sampled.AudioFormat;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

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
    void open(@NotNull AudioFormat format, @NotNull Params params) throws DeviceException;

    /**
     * Reloads the device
     */
    void reload() throws DeviceException;

    /**
     * Closes the device
     */
    void close();

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
     * Process all filters
     *
     * @return the processed samples
     */
    short[] processFilters(short[] samples, @Nullable Predicate<AudioFilter> excludeFilter);

    /**
     * Process all filters
     *
     * @return the processed samples
     */
    default short[] processFilters(short[] samples) {
        return processFilters(samples, null);
    }

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
     * Gets the device params
     *
     * @return the device params
     */
    Optional<Params> getParams();

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
