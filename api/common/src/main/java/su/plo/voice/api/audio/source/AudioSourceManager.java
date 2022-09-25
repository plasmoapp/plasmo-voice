package su.plo.voice.api.audio.source;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AudioSourceManager<T extends AudioSource> {

    Optional<T> getSourceById(@NotNull UUID sourceId);

    Collection<T> getSources();

    void clear();
}
