package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.player.PlayerManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerPermissionUpdateEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class BasePlayerManager implements PlayerManager {

    protected final Map<UUID, VoicePlayer> playerById = Maps.newConcurrentMap();
    protected final Set<String> synchronizedPermissions = new CopyOnWriteArraySet<>();

    @Override
    public Optional<VoicePlayer> getPlayerById(@NotNull UUID playerId) {
        return Optional.ofNullable(playerById.get(playerId));
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
        permissions.put(permission, player.hasPermission(permission));

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
