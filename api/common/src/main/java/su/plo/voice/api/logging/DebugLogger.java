package su.plo.voice.api.logging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.logging.McLogger;

@RequiredArgsConstructor
@Accessors(fluent = true)
public final class DebugLogger implements McLogger {

    private final McLogger logger;
    @Setter
    @Getter
    private boolean enabled;

    /**
     * @deprecated use {@link #info(String, Object...)} instead.
     */
    @Deprecated
    public void log(String message, Object... params) {
        if (!enabled) return;
        logger.info(message, params);
    }

    @Override
    public @NotNull String getName() {
        return logger.getName();
    }

    @Override
    public void trace(@NotNull String format, @NotNull Object... arguments) {
        if (!enabled) return;
        logger.trace(format, arguments);
    }

    @Override
    public void debug(@NotNull String format, @NotNull Object... arguments) {
        if (!enabled) return;
        logger.debug(format, arguments);
    }

    @Override
    public void info(@NotNull String format, @NotNull Object... arguments) {
        if (!enabled) return;
        logger.info(format, arguments);
    }

    @Override
    public void warn(@NotNull String format, @NotNull Object... arguments) {
        if (!enabled) return;
        logger.warn(format, arguments);
    }

    @Override
    public void error(@NotNull String format, @NotNull Object... arguments) {
        if (!enabled) return;
        logger.error(format, arguments);
    }
}
