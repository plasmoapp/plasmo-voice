package su.plo.voice.api.event;

/**
 * Represents an event's priority in execution.
 *
 * <p>
 *     Lower priorities executes first.
 * </p>
 */
public enum EventPriority {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST;

    public static EventPriority byOrdinal(int ordinal) {
        return EventPriority.values()[ordinal];
    }
}
