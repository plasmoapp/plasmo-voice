package su.plo.voice.server.player;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoiceServerPlayer;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class VoiceServerPlayerManager extends BaseVoicePlayerManager<VoiceServerPlayer> {

    private final PlasmoVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public Optional<VoiceServerPlayer> getPlayerById(@NotNull UUID playerId, boolean useServerInstance) {
        VoiceServerPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return minecraftServer.getPlayerById(playerId).map(this::wrap);
    }

    @Override
    public Optional<VoiceServerPlayer> getPlayerByName(@NotNull String playerName, boolean useServerInstance) {
        VoiceServerPlayer voicePlayer = playerByName.get(playerName);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return minecraftServer.getPlayerByName(playerName).map(this::wrap);
    }

    @Override
    public @NotNull VoiceServerPlayer wrap(@NotNull Object instance) {
        return wrap(minecraftServer.getPlayerByInstance(instance));
    }

    private @NotNull VoiceServerPlayer wrap(@NotNull MinecraftServerPlayerEntity serverPlayer) {
        return playerById.computeIfAbsent(
                serverPlayer.getUUID(),
                (playerId) -> {
                    VoiceServerPlayer newPlayer = new VoiceServerPlayerEntity(voiceServer, serverPlayer);
                    playerByName.put(newPlayer.getInstance().getName(), newPlayer);
                    return newPlayer;
                }
        );
    }
}
