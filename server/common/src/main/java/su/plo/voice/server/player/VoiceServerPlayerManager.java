package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.entity.MinecraftServerPlayer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerPermissionUpdateEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public final class VoiceServerPlayerManager implements VoicePlayerManager {

    private final Map<UUID, VoicePlayer> playerById = Maps.newConcurrentMap();
    private final Set<String> synchronizedPermissions = new CopyOnWriteArraySet<>();

    private final PlasmoVoiceServer voiceServer;
    private final MinecraftServerLib minecraftServer;

    @Override
    public Optional<VoicePlayer> getPlayerById(@NotNull UUID playerId) {
        return Optional.ofNullable(playerById.get(playerId));
    }

    @Override
    public @NotNull VoicePlayer wrap(@NotNull Object instance) {
        MinecraftServerPlayer serverPlayer = minecraftServer.getPlayerByInstance(instance);

        return playerById.computeIfAbsent(
                serverPlayer.getUUID(),
                (playerId) -> new VoiceServerPlayer(voiceServer, serverPlayer)
        );
    }

    @Override
    public Collection<VoicePlayer> getPlayers() {
        return playerById.values();
    }

    @Override
    public void registerPermission(@NotNull String permission) {
        if (synchronizedPermissions.contains(permission))
            throw new IllegalArgumentException("Permissions is already registered");

        synchronizedPermissions.add(permission);
    }

    @Override
    public void unregisterPermission(@NotNull String permission) {
        synchronizedPermissions.remove(permission);
    }

    @Override
    public Collection<String> getSynchronizedPermissions() {
        return synchronizedPermissions;
    }

    public void clear() {
        playerById.clear();
        synchronizedPermissions.clear();
    }

    @EventSubscribe
    public void onPermissionUpdate(PlayerPermissionUpdateEvent event) {
        VoicePlayer player = event.getPlayer();
        String permission = event.getPermission();

        Map<String, Boolean> permissions = Maps.newHashMap();
        permissions.put(permission, player.getInstance().hasPermission(permission));

        player.sendPacket(new ConfigPlayerInfoPacket(permissions));
    }

    @EventSubscribe
    public void onPlayerJoin(PlayerJoinEvent event) {
        wrap(event.getPlayer());
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerById.remove(event.getPlayerId());
    }
}
