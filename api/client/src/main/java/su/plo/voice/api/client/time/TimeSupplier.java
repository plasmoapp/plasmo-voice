package su.plo.voice.api.client.time;

/**
 * Functional interface for supplying current time.
 */
@FunctionalInterface
public interface TimeSupplier {

    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    long getCurrentTimeMillis();
}
