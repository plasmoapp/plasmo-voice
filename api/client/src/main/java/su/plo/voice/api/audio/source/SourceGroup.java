package su.plo.voice.api.audio.source;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

// todo: doc
public interface SourceGroup<T extends DeviceSource> {
    void add(@NotNull T source);

    void remove(@NotNull T source);

    void clear();

    Collection<T> getSources();
}
