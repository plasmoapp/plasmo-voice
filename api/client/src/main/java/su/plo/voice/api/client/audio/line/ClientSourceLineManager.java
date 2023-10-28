package su.plo.voice.api.client.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.line.SourceLineManager;
import su.plo.voice.proto.data.audio.line.SourceLine;

import java.util.Collection;

/**
 * Manages client audio source lines.
 */
public interface ClientSourceLineManager extends SourceLineManager<ClientSourceLine> {

    /**
     * Registers a client source line.
     *
     * @param line The client source line to register.
     * @return The registered client source line.
     */
    @NotNull ClientSourceLine register(@NotNull ClientSourceLine line);

    /**
     * Registers a general source line as a client source line.
     *
     * @param line The source line to register as a client source line.
     * @return The registered client source line.
     */
    @NotNull ClientSourceLine register(@NotNull SourceLine line);

    /**
     * Registers a collection of source lines as client source lines.
     *
     * @param lines The collection of source lines to register as client source lines.
     * @return A collection of the registered client source lines.
     */
    @NotNull Collection<ClientSourceLine> register(@NotNull Collection<SourceLine> lines);
}
