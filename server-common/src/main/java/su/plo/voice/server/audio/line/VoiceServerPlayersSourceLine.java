package su.plo.voice.server.audio.line;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.audio.line.ServerPlayersSet;
import su.plo.voice.api.server.audio.line.ServerPlayersSourceLine;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket;

import java.util.*;
import java.util.stream.Collectors;

@ToString(callSuper = true)
public final class VoiceServerPlayersSourceLine
        extends VoiceServerSourceLine
        implements ServerPlayersSourceLine {

    private final Map<UUID, ServerPlayersSet> playersSets = Maps.newConcurrentMap();

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
                getPlayersSet(player)
                        .getPlayers()
                        .stream()
                        .map((linePlayer) -> linePlayer.getInstance().getGameProfile())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public void setPlayersSet(@NotNull VoicePlayer player, @Nullable ServerPlayersSet playersSet) {
        if (playersSet == null) {
            playersSets.remove(player.getInstance().getUUID());
            player.sendPacket(new SourceLinePlayersListPacket(id, Collections.emptyList()));
            return;
        }

        playersSets.put(player.getInstance().getUUID(), playersSet);
    }

    @Override
    public @NotNull ServerPlayersSet getPlayersSet(@NotNull VoicePlayer player) {
        return playersSets.computeIfAbsent(
                player.getInstance().getUUID(),
                (playerId) -> new VoicePlayersSet(player)
        );
    }

    @Override
    public @NotNull ServerPlayersSet createBroadcastSet() {
        return new VoiceBroadcastPlayersSet();
    }

    @EventSubscribe
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playersSets.remove(event.getPlayerId());
    }

    private class VoiceBroadcastPlayersSet implements ServerPlayersSet {

        private final Set<VoicePlayer> players = Sets.newCopyOnWriteArraySet();

        @Override
        public void addPlayer(@NotNull VoicePlayer player) {
            if (players.contains(player)) return;
            broadcast(new SourceLinePlayerAddPacket(id, player.getInstance().getGameProfile()));
            players.add(player);

            player.sendPacket(new SourceLinePlayersListPacket(
                    id,
                    players.stream()
                            .map(linePlayer -> linePlayer.getInstance().getGameProfile())
                            .collect(Collectors.toList())
                    ));
        }

        @Override
        public boolean removePlayer(@NotNull UUID playerId) {
            return players.stream()
                    .filter((player) -> player.getInstance().getUUID().equals(playerId))
                    .findFirst()
                    .map((player) -> {
                        broadcast(new SourceLinePlayerRemovePacket(id, playerId));
                        players.remove(player);
                        return true;
                    })
                    .orElse(false);
        }

        @Override
        public void clearPlayers() {
            players.forEach(player -> {
                player.sendPacket(new SourceLinePlayersListPacket(id, Collections.emptyList()));
            });
            players.clear();
        }

        @Override
        public Collection<VoicePlayer> getPlayers() {
            return players;
        }

        private void broadcast(@NotNull Packet<?> packet) {
            players.forEach(player -> player.sendPacket(packet));
        }
    }

    @RequiredArgsConstructor
    private class VoicePlayersSet implements ServerPlayersSet {

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
