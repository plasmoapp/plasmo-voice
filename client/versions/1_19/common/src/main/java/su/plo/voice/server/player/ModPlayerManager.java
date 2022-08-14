package su.plo.voice.server.player;

import lombok.AllArgsConstructor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public final class ModPlayerManager extends BasePlayerManager {

    private final PlasmoVoiceServer voiceServer;
    private final MinecraftServer server;

    @Override
    public Optional<VoicePlayer> getPlayerById(@NotNull UUID playerId) {
        Optional<VoicePlayer> player = super.getPlayerById(playerId);
        if (player.isEmpty()) {
            ServerPlayer serverPlayer = server.getPlayerList().getPlayer(playerId);
            if (serverPlayer == null) return Optional.empty();

            VoicePlayer voicePlayer = new ModVoicePlayer(voiceServer, serverPlayer);
            playerById.put(serverPlayer.getUUID(), voicePlayer);
        }

        return player;
    }

    @Override
    public @NotNull VoicePlayer wrap(@NotNull Object player) {
        if (!(player instanceof ServerPlayer serverPlayer))
            throw new IllegalArgumentException("player is not " + ServerPlayer.class);

        if (playerById.containsKey(serverPlayer.getUUID()))
            return playerById.get(serverPlayer.getUUID());

        VoicePlayer voicePlayer = new ModVoicePlayer(voiceServer, serverPlayer);
        playerById.put(serverPlayer.getUUID(), voicePlayer);

        return voicePlayer;
    }
}
