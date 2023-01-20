package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSourceManager;

import java.util.UUID;

public interface BaseServerSourceManager<S extends AudioSource<?, ?>> extends AudioSourceManager<S> {

    void remove(@NotNull UUID sourceId);

    void remove(@NotNull S source);
}
