package su.plo.voice.api.audio.line;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.line.SourceLine;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SourceLineManager<T extends SourceLine> {

    Optional<T> getLineById(@NotNull UUID id);

    Optional<T> getLineByName(@NotNull String name);

    Collection<T> getLines();

    boolean unregister(@NotNull UUID id);

    boolean unregister(@NotNull String name);

    boolean unregister(@NotNull T line);
}
