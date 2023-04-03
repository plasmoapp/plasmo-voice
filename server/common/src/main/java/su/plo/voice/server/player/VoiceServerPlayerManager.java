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

        return minecraftServer.getPlayerById(playerId)
                .map((player) -> playerById.computeIfAbsent(
                        player.getUUID(),
                        (pId) -> {
                            VoiceServerPlayer newPlayer = new VoiceServerPlayerEntity(voiceServer, player);
                            playerByName.put(newPlayer.getInstance().getName(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public Optional<VoiceServerPlayer> getPlayerByName(@NotNull String playerName, boolean useServerInstance) {
        VoiceServerPlayer voicePlayer = playerByName.get(playerName);
        if (voicePlayer != null) return Optional.of(voicePlayer);
        else if (!useServerInstance) return Optional.empty();

        return minecraftServer.getPlayerByName(playerName)
                .map((player) -> playerByName.computeIfAbsent(
                        player.getName(),
                        (pId) -> {
                            VoiceServerPlayer newPlayer = new VoiceServerPlayerEntity(voiceServer, player);
                            playerById.put(newPlayer.getInstance().getUUID(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public @NotNull VoiceServerPlayer wrap(@NotNull Object instance) {
        MinecraftServerPlayerEntity serverPlayer = minecraftServer.getPlayerByInstance(instance);

        return playerById.computeIfAbsent(
                serverPlayer.getUUID(),
                (playerId) -> new VoiceServerPlayerEntity(voiceServer, serverPlayer)
        );
    }
}
