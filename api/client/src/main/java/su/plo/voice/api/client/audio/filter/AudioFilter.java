package su.plo.voice.api.client.audio.filter;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an audio filter that can modify audio data before it is played or transmitted.
 */
public interface AudioFilter {

    /**
     * Gets the name of the audio filter.
     *
     * @return The name of the filter.
     */
    @NotNull String getName();

    /**
     * Processes the audio filter on the given audio samples.
     * <br/>
     * Implementations of this method may modify the original array of audio samples.
     *
     * @param samples The audio samples to process.
     * @return The processed audio samples.
     */
    short[] process(@NotNull AudioFilterContext context, short[] samples);

    /**
     * Checks if the audio filter is enabled.
     *
     * @return {@code true} if the filter is enabled, {@code false} otherwise.
     */
    boolean isEnabled();

    /**
     * Gets the number of required device channels to process this filter.
     * A value of "0" indicates that the number of channels is not required, so it will work always.
     *
     * @return The number of required device channels.
     */
    default int getSupportedChannels() {
        return 0;
    }

    /**
     * Represents the priority of the audio filter in execution.
     *
     * <p>
     *     Lower priorities executes first.
     * </p>
     */
    enum Priority {
        LOW,
        LOWEST,
        NORMAL,
        HIGH,
        HIGHEST;

        public static Priority byOrdinal(int ordinal) {
            return Priority.values()[ordinal];
        }
    }
}
