package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;

import java.util.UUID;

public interface ServerSourceManager extends AudioSourceManager<ServerAudioSource> {

    @NotNull ServerPlayerSource getOrCreatePlayerSource(@Nullable Object addonObject,
                                                        @NotNull VoicePlayer player,
                                                        @Nullable String codec,
                                                        boolean stereo);

    @NotNull ServerEntitySource getOrCreateEntitySource(@Nullable Object addonObject,
                                                        @NotNull VoiceEntity entity,
                                                        @Nullable String codec,
                                                        boolean stereo);

    @NotNull ServerStaticSource createStaticSource(@NotNull Object addonObject,
                                                   @NotNull ServerPos3d position,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                   @NotNull VoicePlayer player,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull UUID registerCustomSource(@NotNull ServerAudioSource source);
}
