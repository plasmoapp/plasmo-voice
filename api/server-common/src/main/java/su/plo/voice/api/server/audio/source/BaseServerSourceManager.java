package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.audio.line.ServerSourceLine;

import java.util.UUID;

public interface BaseServerSourceManager extends AudioSourceManager<ServerAudioSource<?>> {

    @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    void remove(@NotNull UUID sourceId);

    void remove(@NotNull ServerAudioSource<?> source);
}
