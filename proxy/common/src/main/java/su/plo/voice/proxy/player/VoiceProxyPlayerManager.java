package su.plo.voice.proxy.player;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.server.player.BaseVoicePlayerManager;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * This manager can be used to get voice players
 */
@RequiredArgsConstructor
public final class VoiceProxyPlayerManager
        extends BaseVoicePlayerManager<VoiceProxyPlayer>
        implements su.plo.voice.api.server.player.VoiceProxyPlayerManager {

    private final PlasmoVoiceProxy voiceProxy;
    private final MinecraftProxyLib minecraftProxy;

    @Override
    public Optional<VoiceProxyPlayer> getPlayerById(@NotNull UUID playerId) {
        VoiceProxyPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);

        return minecraftProxy.getPlayerById(playerId)
                .map((player) -> playerById.computeIfAbsent(
                        player.getUUID(),
                        (pId) -> {
                            VoiceProxyPlayer newPlayer = new VoiceProxyPlayerConnection(voiceProxy, player);

                            playerByName.put(newPlayer.getInstance().getName(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public Optional<VoiceProxyPlayer> getPlayerByName(@NotNull String playerName) {
        VoiceProxyPlayer voicePlayer = playerByName.get(playerName);
        if (voicePlayer != null) return Optional.of(voicePlayer);

        return minecraftProxy.getPlayerByName(playerName)
                .map((player) -> playerByName.computeIfAbsent(
                        player.getName(),
                        (pId) -> {
                            VoiceProxyPlayer newPlayer = new VoiceProxyPlayerConnection(voiceProxy, player);

                            playerById.put(newPlayer.getInstance().getUUID(), newPlayer);
                            return newPlayer;
                        }
                ));
    }

    @Override
    public @NotNull VoiceProxyPlayer wrap(@NotNull Object instance) {
        MinecraftProxyPlayer serverPlayer = minecraftProxy.getPlayerByInstance(instance);

        return playerById.computeIfAbsent(
                serverPlayer.getUUID(),
                (playerId) -> new VoiceProxyPlayerConnection(voiceProxy, serverPlayer)
        );
    }

    @Override
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoiceProxyPlayer> filter) {
        for (VoiceProxyPlayer player : getPlayers()) {
            if ((filter == null || filter.test(player)) && player.hasVoiceChat())
                player.sendPacket(packet);
        }
    }

    @Override
    public void onPlayerJoin(@NotNull MinecraftServerPlayer player) {
        super.onPlayerJoin(player);
    }

    @Override
    public void onPlayerQuit(@NotNull MinecraftServerPlayer player) {
        super.onPlayerQuit(player);
    }
}
