package su.plo.voice.server.player;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.api.server.entity.player.McServerPlayer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.player.VoiceServerPlayerManager;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class VoiceServerPlayerManagerImpl
        extends BaseVoicePlayerManager<VoiceServerPlayer>
        implements VoiceServerPlayerManager
{

    private final PlasmoVoiceServer voiceServer;
    private final McServerLib minecraftServer;

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

    private @NotNull VoiceServerPlayer wrap(@NotNull McServerPlayer serverPlayer) {
        return playerById.computeIfAbsent(
                serverPlayer.getUuid(),
                (playerId) -> {
                    VoiceServerPlayer newPlayer = new VoiceServerPlayerEntity(voiceServer, serverPlayer);
                    playerByName.put(newPlayer.getInstance().getName(), newPlayer);
                    return newPlayer;
                }
        );
    }
}
