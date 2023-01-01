package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.UUID;

public interface ServerSourceManager extends AudioSourceManager<ServerAudioSource<?>> {

    @NotNull ServerPlayerSource createPlayerSource(@Nullable Object addonObject,
                                                   @NotNull VoicePlayer player,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerEntitySource createEntitySource(@Nullable Object addonObject,
                                                   @NotNull MinecraftServerEntity entity,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerStaticSource createStaticSource(@NotNull Object addonObject,
                                                   @NotNull ServerPos3d position,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                   @NotNull ServerSourceLine line,
                                                   @Nullable String codec,
                                                   boolean stereo);

    void remove(@NotNull UUID sourceId);

    void remove(@NotNull AudioSource<?> source);
}
