package su.plo.voice.server.audio.source;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.source.*;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.source.SourceInfo;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class VoiceServerSourceManager implements ServerSourceManager {

    private final PlasmoVoiceServer voiceServer;

    private final Map<UUID, ServerAudioSource> sourceById = Maps.newConcurrentMap();
    private final Map<UUID, ServerPlayerSource> sourceByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, ServerEntitySource> sourceByEntityId = Maps.newConcurrentMap();

    @Override
    public Optional<ServerAudioSource> getSourceById(@NotNull UUID sourceId) {
        return Optional.ofNullable(sourceById.get(sourceId));
    }

    public Collection<ServerAudioSource> getSources(@Nullable SourceInfo.Type type) {
        return sourceById.values();
    }

    @Override
    public @NotNull ServerPlayerSource getOrCreatePlayerSource(@NotNull VoicePlayer player, @Nullable String codec) {
        return sourceByPlayerId.computeIfAbsent(player.getUUID(), (playerId) -> {
            ServerPlayerSource source = new VoiceServerPlayerSource(
                    voiceServer.getUdpConnectionManager(),
                    codec,
                    player
            );

            sourceById.put(source.getId(), source);

            return source;
        });
    }

    @Override
    public @NotNull ServerEntitySource getOrCreateEntitySource(@NotNull VoiceEntity entity, @Nullable String codec) {
        return sourceByEntityId.computeIfAbsent(entity.getUUID(), (playerId) -> {
            ServerEntitySource source = new VoiceServerEntitySource(
                    voiceServer.getUdpConnectionManager(),
                    codec,
                    entity
            );

            sourceById.put(source.getId(), source);

            return source;
        });
    }

    @Override
    public @NotNull ServerStaticSource createStaticSource(@NotNull ServerPos3d position, @Nullable String codec) {
        ServerStaticSource source = new VoiceServerStaticSource(voiceServer.getUdpConnectionManager(), codec, position);
        sourceById.put(source.getId(), source);

        return source;
    }

    @Override
    public @NotNull ServerDirectSource createDirectSource(@NotNull VoicePlayer player, @Nullable String codec) {
        ServerDirectSource source = new VoiceServerDirectSource(voiceServer.getUdpConnectionManager(), codec, player);
        sourceById.put(source.getId(), source);

        return source;
    }

    @Override
    public UUID registerCustomSource(@NotNull ServerAudioSource source) {
        UUID sourceId = UUID.randomUUID();
        sourceById.put(sourceId, source);

        return sourceId;
    }

    @EventSubscribe
    public void onVoiceShutdown(VoiceServerShutdownEvent event) {
        sourceById.clear();
        sourceByEntityId.clear();
        sourceByPlayerId.clear();
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        ServerAudioSource source = sourceByPlayerId.remove(event.getPlayerId());
        if (source == null) return;

        sourceById.remove(source.getId());
    }
}
