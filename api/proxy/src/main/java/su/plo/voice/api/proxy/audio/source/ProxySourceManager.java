package su.plo.voice.api.proxy.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.BaseServerSourceManager;

public interface ProxySourceManager extends BaseServerSourceManager<ProxyAudioSource<?>> {

    @NotNull ProxyDirectSource createDirectSource(@NotNull Object addonObject,
                                                                    @NotNull ServerSourceLine line,
                                                                    @Nullable String codec,
                                                                    boolean stereo);
}
