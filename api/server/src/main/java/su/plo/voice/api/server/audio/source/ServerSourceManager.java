package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;

public interface ServerSourceManager extends AudioSourceManager<ServerAudioSource> {

    @NotNull ServerPlayerSource createPlayerSource(@Nullable Object addonObject,
                                                   @NotNull VoicePlayer player,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerEntitySource createEntitySource(@Nullable Object addonObject,
                                                   @NotNull VoiceEntity entity,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerStaticSource createStaticSource(@NotNull Object addonObject,
                                                   @NotNull ServerPos3d position,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                   @NotNull VoicePlayer player,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);
}
