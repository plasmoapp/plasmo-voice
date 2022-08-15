package su.plo.voice.api.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.data.source.SourceInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AudioSourceManager<T extends AudioSource> {

    Optional<T> getSourceById(@NotNull UUID sourceId);

    Collection<T> getSources(@Nullable SourceInfo.Type type);
}
