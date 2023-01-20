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
    public Optional<VoiceServerPlayer> getPlayerById(@NotNull UUID playerId) {
        VoiceServerPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);

        return minecraftServer.getPlayerById(playerId)
                .map((player) -> playerById.computeIfAbsent(
                        player.getUUID(),
                        (pId) -> new VoiceServerPlayerEntity(voiceServer, player)
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
