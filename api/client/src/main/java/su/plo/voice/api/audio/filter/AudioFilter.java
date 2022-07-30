package su.plo.voice.api.audio.filter;

/**
 * Audio filters can modify audio before it will be played or sent
 */
public interface AudioFilter {

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
