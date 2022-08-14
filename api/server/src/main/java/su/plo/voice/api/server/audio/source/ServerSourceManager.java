package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.audio.source.AudioSourceManager;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;

public interface ServerSourceManager extends AudioSourceManager<ServerAudioSource> {

    @NotNull ServerPlayerSource getOrCreatePlayerSource(@NotNull VoicePlayer player, @NotNull String codec);

    @NotNull ServerEntitySource getOrCreateEntitySource(@NotNull VoiceEntity entity, @NotNull String codec);

    @NotNull ServerStaticSource createStaticSource(@NotNull ServerPos3d position, @NotNull String codec);

    int registerCustomSource(@NotNull AudioSource source);
}
