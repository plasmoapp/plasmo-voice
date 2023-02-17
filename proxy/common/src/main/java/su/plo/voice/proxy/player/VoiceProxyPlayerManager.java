package su.plo.voice.proxy.player;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.proxy.MinecraftProxyLib;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.player.VoiceProxyPlayer;
import su.plo.voice.api.server.connection.ConnectionManager;
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
        implements ConnectionManager<ClientPacketTcpHandler, VoiceProxyPlayer> {

    private final PlasmoVoiceProxy voiceProxy;
    private final MinecraftProxyLib minecraftProxy;

    /**
     * Gets the {@link VoiceProxyPlayer} by uuid
     *
     * @param playerId player's unique id
     * @return {@link VoiceProxyPlayer}
     */
    public Optional<VoiceProxyPlayer> getPlayerById(@NotNull UUID playerId) {
        VoiceProxyPlayer voicePlayer = playerById.get(playerId);
        if (voicePlayer != null) return Optional.of(voicePlayer);

        return minecraftProxy.getPlayerById(playerId)
                .map((player) -> playerById.computeIfAbsent(
                        player.getUUID(),
                        (pId) -> new VoiceProxyPlayerConnection(voiceProxy, player)
                ));
    }

    /**
     * Gets the {@link VoiceProxyPlayer} by server player
     *
     * @param instance player's server object
     * @return {@link VoiceProxyPlayer}
     */
    public @NotNull VoiceProxyPlayer wrap(@NotNull Object instance) {
        MinecraftProxyPlayer serverPlayer = minecraftProxy.getPlayerByInstance(instance);

        return playerById.computeIfAbsent(
                serverPlayer.getUUID(),
                (playerId) -> new VoiceProxyPlayerConnection(voiceProxy, serverPlayer)
        );
    }

    /**
     * Broadcasts packet to all players with voice chat installed
     */
    public void broadcast(@NotNull Packet<ClientPacketTcpHandler> packet, @Nullable Predicate<VoiceProxyPlayer> filter) {
        for (VoiceProxyPlayer player : getPlayers()) {
            if ((filter == null || filter.test(player)) && player.hasVoiceChat())
                player.sendPacket(packet);
        }
    }
}
