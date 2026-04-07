package su.plo.voice.api.client.time;

/**
 * Interface for supplying current time.
 */
public interface TimeSupplier {
    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    long getCurrentTimeMillis();

    /**
     * Gets the current value of a monotonic clock in nanoseconds.
     *
     * <p>
     *     The value has no fixed origin and is only meaningful for measuring elapsed time
     *     between two calls on the same supplier.
     * </p>
     *
     * @return The current monotonic time in nanoseconds.
     */
    default long getNanoTime() {
        return getCurrentTimeMillis() * 1_000_000L;
    }
}
