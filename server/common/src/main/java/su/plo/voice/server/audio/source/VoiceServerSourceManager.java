package su.plo.voice.server.audio.source;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.audio.source.*;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.api.server.player.VoiceServerPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class VoiceServerSourceManager implements ServerSourceManager {

    private final PlasmoVoiceServer voiceServer;

    private final Map<UUID, ServerAudioSource<?>> sourceById = Maps.newConcurrentMap();
    private final Map<UUID, ServerPlayerSource> sourceByPlayerId = Maps.newConcurrentMap();
    private final Map<UUID, ServerEntitySource> sourceByEntityId = Maps.newConcurrentMap();

    @Override
    public Optional<ServerAudioSource<?>> getSourceById(@NotNull UUID sourceId) {
        return Optional.ofNullable(sourceById.get(sourceId));
    }

    public Collection<ServerAudioSource<?>> getSources() {
        return sourceById.values();
    }

    @Override
    public void clear() {
        // todo: create & send source removed packet

        sourceByEntityId.clear();
        sourceByPlayerId.clear();
        sourceById.clear();
    }

    @Override
    public @NotNull ServerPlayerSource createPlayerSource(@Nullable Object addonObject,
                                                          @NotNull VoiceServerPlayer player,
                                                          @NotNull ServerSourceLine line,
                                                          @Nullable String codec,
                                                          boolean stereo) {
        return sourceByPlayerId.computeIfAbsent(player.getInstance().getUUID(), (playerId) -> {
            Optional<AddonContainer> addon = voiceServer.getAddonManager().getAddon(addonObject);
            if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

            ServerPlayerSource source = new VoiceServerPlayerSource(
                    voiceServer,
                    addon.get(),
                    line,
                    codec,
                    stereo,
                    player
            );

            sourceById.put(source.getId(), source);

            return source;
        });
    }

    @Override
    public @NotNull ServerEntitySource createEntitySource(@Nullable Object addonObject,
                                                          @NotNull MinecraftServerEntity entity,
                                                          @NotNull ServerSourceLine line,
                                                          @Nullable String codec,
                                                          boolean stereo) {
        return sourceByEntityId.computeIfAbsent(entity.getUUID(), (playerId) -> {
            Optional<AddonContainer> addon = voiceServer.getAddonManager().getAddon(addonObject);
            if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

            ServerEntitySource source = new VoiceServerEntitySource(
                    voiceServer,
                    addon.get(),
                    line,
                    codec,
                    stereo,
                    entity
            );

            sourceById.put(source.getId(), source);

            return source;
        });
    }

    @Override
    public @NotNull ServerStaticSource createStaticSource(@NotNull Object addonObject,
                                                          @NotNull ServerPos3d position,
                                                          @NotNull ServerSourceLine line,
                                                          @Nullable String codec,
                                                          boolean stereo) {
        Optional<AddonContainer> addon = voiceServer.getAddonManager().getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        ServerStaticSource source = new VoiceServerStaticSource(
                voiceServer,
                addon.get(),
                line,
                codec,
                stereo,
                position
        );
        sourceById.put(source.getId(), source);

        return source;
    }

    @Override
    public @NotNull ServerDirectSource createDirectSource(@NotNull Object addonObject,
                                                                            @NotNull ServerSourceLine line,
                                                                            @Nullable String codec,
                                                                            boolean stereo) {
        Optional<AddonContainer> addon = voiceServer.getAddonManager().getAddon(addonObject);
        if (!addon.isPresent()) throw new IllegalArgumentException("addonObject is not an addon");

        ServerDirectSource source = new VoiceServerAudioDirectSource(
                voiceServer,
                voiceServer.getUdpConnectionManager(),
                addon.get(),
                line,
                codec,
                stereo
        );
        sourceById.put(source.getId(), source);

        return source;
    }

    @Override
    public void remove(@NotNull UUID sourceId) {
        AudioSource<?, VoiceServerPlayer> source = sourceById.remove(sourceId);
        if (source instanceof ServerPlayerSource) {
            sourceByPlayerId.remove(sourceId);
        } else if (source instanceof ServerEntitySource) {
            sourceByEntityId.remove(sourceId);
        }
    }

    @Override
    public void remove(@NotNull ServerAudioSource<?> source) {
        remove(source.getId());
    }

//    @Override
//    public @NotNull UUID registerCustomSource(@NotNull ServerAudioSource source) {
//        UUID sourceId = UUID.randomUUID();
//        sourceById.put(sourceId, source);
//
//        return sourceId;
//    }

    @EventSubscribe
    public void onVoiceShutdown(VoiceServerShutdownEvent event) {
        sourceById.clear();
        sourceByEntityId.clear();
        sourceByPlayerId.clear();
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        AudioSource source = sourceByPlayerId.remove(event.getPlayerId());
        if (source == null) return;

        sourceById.remove(source.getId());
    }
}
