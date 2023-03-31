package su.plo.voice.api.client.audio.filter;

import org.jetbrains.annotations.NotNull;

/**
 * Audio filters can modify audio before it will be played or sent
 */
public interface AudioFilter {

    /**
     * Gets the filter name
     *
     * @return the filter name
     */
    @NotNull String getName();

    /**
     * Process the audio filter
     * (!) Can change an original array
     */
    short[] process(short[] samples);

    /**
     * @return true if the filter is enabled
     */
    boolean isEnabled();

    /**
     * @return number of required device channels to process this filter. With "0" this will be ignored
     */
    default int getSupportedChannels() {
        return 0;
    }

    /**
     * Represents the filter priority in execution
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
