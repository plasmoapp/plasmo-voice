package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoiceServerPlayer;

public interface ServerSourceManager extends BaseServerSourceManager<ServerAudioSource<?>> {

    @NotNull ServerPlayerSource createPlayerSource(@Nullable Object addonObject,
                                                   @NotNull VoiceServerPlayer player,
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
}
