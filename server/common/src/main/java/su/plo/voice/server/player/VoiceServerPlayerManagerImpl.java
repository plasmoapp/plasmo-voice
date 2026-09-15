package su.plo.voice.server.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.player.VoiceServerPlayerManager;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class VoiceServerPlayerManagerImpl
        extends BaseVoicePlayerManager<VoiceServerPlayer>
        implements VoiceServerPlayerManager
{

    private final PlasmoVoiceServer voiceServer;
    private final McServerLib minecraftServer;

    private final Cache<UUID, Integer> quitEntityIdByPlayerId = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Override
    public Optional<VoiceServerPlayer> getPlayerById(@NotNull UUID playerId, boolean useServerInstance) {
        VoiceServerPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return Optional.ofNullable(minecraftServer.getPlayerById(playerId))
                .map(this::wrap);
    }

    @Override
    public Optional<VoiceServerPlayer> getPlayerByName(@NotNull String playerName, boolean useServerInstance) {
        VoiceServerPlayer voicePlayer = playerByName.get(playerName);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return Optional.ofNullable(minecraftServer.getPlayerByName(playerName))
                .map(this::wrap);
    }

    @Override
    public @NotNull VoiceServerPlayer getPlayerByInstance(@NotNull Object instance) {
        return wrap(minecraftServer.getPlayerByInstance(instance));
    }

    @Override
    public void onPlayerQuit(@NotNull McPlayer player) {
        quitEntityIdByPlayerId.put(player.getUuid(), ((McServerPlayer) player).getId());

        super.onPlayerQuit(player);
    }

    private @NotNull VoiceServerPlayer wrap(@NotNull McServerPlayer serverPlayer) {
        if (!serverPlayer.isOnline()) {
            BaseVoice.DEBUG_LOGGER.warn("Wrapping offline {} (entityId: {}), minecraft server returned a stale instance",
                    serverPlayer.getName(),
                    serverPlayer.getId()
            );
            if (BaseVoice.DEBUG_LOGGER.enabled()) {
                Thread.dumpStack();
            }

            return new VoiceServerPlayerEntity(voiceServer, serverPlayer);
        }

        VoiceServerPlayer voicePlayer = playerById.compute(
                serverPlayer.getUuid(),
                (playerId, storedPlayer) -> {
                    Integer quitEntityId = quitEntityIdByPlayerId.getIfPresent(playerId);
                    if (quitEntityId != null && quitEntityId == serverPlayer.getId()) {
                        if (storedPlayer != null)
                            playerByName.remove(storedPlayer.getInstance().getName(), storedPlayer);

                        return null;
                    }

                    if (storedPlayer != null && storedPlayer.getInstance().getId() == serverPlayer.getId())
                        return storedPlayer;

                    if (storedPlayer != null) {
                        BaseVoice.DEBUG_LOGGER.warn("Replacing stale {} in the player manager (entityId: {} -> {})",
                                serverPlayer.getName(),
                                storedPlayer.getInstance().getId(),
                                serverPlayer.getId()
                        );
                        playerByName.remove(storedPlayer.getInstance().getName(), storedPlayer);
                    }

                    quitEntityIdByPlayerId.invalidate(playerId);

                    VoiceServerPlayer newPlayer = new VoiceServerPlayerEntity(voiceServer, serverPlayer);
                    playerByName.put(serverPlayer.getName(), newPlayer);
                    return newPlayer;
                }
        );

        if (voicePlayer != null) return voicePlayer;

        BaseVoice.DEBUG_LOGGER.warn("Wrapping quit {} (entityId: {}) without storing it",
                serverPlayer.getName(),
                serverPlayer.getId()
        );
        if (BaseVoice.DEBUG_LOGGER.enabled()) {
            Thread.dumpStack();
        }

        return new VoiceServerPlayerEntity(voiceServer, serverPlayer);
    }
}
