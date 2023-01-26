package su.plo.voice.server.audio.line;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.line.ServerPlayerMap;
import su.plo.voice.api.server.audio.line.ServerPlayersSourceLine;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ToString(callSuper = true)
public final class VoiceServerPlayersSourceLine extends VoiceServerSourceLine implements ServerPlayersSourceLine {

    private final Map<UUID, ServerPlayerMap> playerMaps = Maps.newConcurrentMap();

    public VoiceServerPlayersSourceLine(@NotNull AddonContainer addon,
                                        @NotNull String name,
                                        @NotNull String translation,
                                        @NotNull String icon,
                                        int weight) {
        super(addon, name, translation, icon, weight);
    }

    @Override
    public @NotNull VoiceSourceLine getPlayerSourceLine(@NotNull VoicePlayer player) {
        return new VoiceSourceLine(
                name,
                translation,
                icon,
                weight,
                getPlayerMap(player)
                        .getPlayers()
                        .stream()
                        .map((linePlayer) -> linePlayer.getInstance().getGameProfile())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public void setPlayerMap(@NotNull VoicePlayer player, @NotNull ServerPlayerMap playerMap) {
        playerMaps.put(player.getInstance().getUUID(), playerMap);
    }

    @Override
    public @NotNull ServerPlayerMap getPlayerMap(@NotNull VoicePlayer player) {
        return playerMaps.computeIfAbsent(
                player.getInstance().getUUID(),
                (playerId) -> new VoicePlayerMap(player)
        );
    }

    @EventSubscribe
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playerMaps.remove(event.getPlayerId());
    }

    @RequiredArgsConstructor
    private class VoicePlayerMap implements ServerPlayerMap {

        private final VoicePlayer player;
        private final Set<VoicePlayer> players = Sets.newCopyOnWriteArraySet();

        @Override
        public void addPlayer(@NotNull VoicePlayer player) {
            players.add(player);
            player.sendPacket(new SourceLinePlayerAddPacket(id, player.getInstance().getGameProfile()));
        }

        @Override
        public boolean removePlayer(@NotNull UUID playerId) {
            return players.stream()
                    .filter((player) -> player.getInstance().getUUID().equals(playerId))
                    .findFirst()
                    .map((player) -> {
                        player.sendPacket(new SourceLinePlayerRemovePacket(id, playerId));
                        players.remove(player);
                        return true;
                    })
                    .orElse(false);
        }

        @Override
        public void clearPlayers() {
            player.sendPacket(new SourceLinePlayersListPacket(id));
            players.clear();
        }

        @Override
        public Collection<VoicePlayer> getPlayers() {
            return players;
        }
    }
}
