package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;

import java.util.UUID;

public interface ServerSourceManager extends AudioSourceManager<ServerAudioSource> {

    @NotNull ServerPlayerSource getOrCreatePlayerSource(@NotNull VoicePlayer player, @Nullable String codec);

    @NotNull ServerEntitySource getOrCreateEntitySource(@NotNull VoiceEntity entity, @Nullable String codec);

    @NotNull ServerStaticSource createStaticSource(@NotNull ServerPos3d position, @Nullable String codec);

    @NotNull UUID registerCustomSource(@NotNull ServerAudioSource source);
}
