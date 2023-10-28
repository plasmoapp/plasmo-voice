package su.plo.voice.api.client.audio.device.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.DeviceException;

import java.util.Collection;

/**
 * {@link SourceGroup} is responsible for creating audio sources for all opened output devices.
 */
public interface SourceGroup {

    /**
     * Creates audio sources for the opened output devices.
     *
     * @param stereo Indicates whether stereo sources should be created.
     * @param params Additional parameters for source creation.
     * @throws DeviceException If there is an issue with opening an audio source.
     */
    void create(boolean stereo, @NotNull DeviceSourceParams params) throws DeviceException;

    /**
     * Closes and clears all existing audio sources in the group.
     */
    void clear();

    /**
     * Gets a collection of audio sources created within the group.
     *
     * @return A collection of audio sources.
     */
    @NotNull Collection<DeviceSource> getSources();
}
