package su.plo.voice.server.audio.line;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.audio.line.PlayerMap;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.line.ServerPlayersSourceLine;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersClearPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ToString(callSuper = true)
public final class VoiceServerPlayersSourceLine extends VoiceServerSourceLine implements ServerPlayersSourceLine {

    private final Map<UUID, PlayerMap> playerMaps = Maps.newConcurrentMap();

    public VoiceServerPlayersSourceLine(@NotNull AddonContainer addon,
                                        @NotNull String name,
                                        @NotNull String translation,
                                        @NotNull String icon,
                                        int weight) {
        super(addon, name, translation, icon, weight);
    }

    @Override
    public @NotNull VoiceSourceLine getPlayerSourceLine(@NotNull VoicePlayer<?> player) {
        return new VoiceSourceLine(
                name,
                translation,
                icon,
                weight,
                Sets.newHashSet(playerMaps.get(player.getInstance().getUUID()).getPlayers())
        );
    }

    @Override
    public void setPlayerMap(@NotNull VoicePlayer<?> player, @NotNull PlayerMap playerMap) {
        playerMaps.put(player.getInstance().getUUID(), playerMap);
    }

    @Override
    public @NotNull PlayerMap getPlayerMap(@NotNull VoicePlayer<?> player) {
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
    private class VoicePlayerMap implements PlayerMap {

        private final VoicePlayer<?> player;
        private final Set<UUID> players = Sets.newCopyOnWriteArraySet();

        @Override
        public void addPlayer(@NotNull UUID playerId) {
            players.add(playerId);
            player.sendPacket(new SourceLinePlayerAddPacket(id, playerId));
        }

        @Override
        public boolean removePlayer(@NotNull UUID playerId) {
            if (players.contains(playerId)) {
                player.sendPacket(new SourceLinePlayerRemovePacket(id, playerId));

                players.remove(playerId);
                return true;
            }

            return false;
        }

        @Override
        public void clearPlayers() {
            player.sendPacket(new SourceLinePlayersClearPacket(id));
            players.clear();
        }

        @Override
        public Collection<UUID> getPlayers() {
            return players;
        }
    }
}
