package su.plo.voice.server.player;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventPriority;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.event.player.PlayerJoinEvent;
import su.plo.voice.api.server.event.player.PlayerPermissionUpdateEvent;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.player.VoicePlayerManager;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public abstract class BaseVoicePlayerManager<P extends VoicePlayer> implements VoicePlayerManager<P> {

    protected final Map<UUID, P> playerById = Maps.newConcurrentMap();
    protected final Set<String> synchronizedPermissions = new CopyOnWriteArraySet<>();

    @Override
    public Collection<P> getPlayers() {
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
    public void onPermissionUpdate(@NotNull PlayerPermissionUpdateEvent event) {
        String permission = event.getPermission();

        if (synchronizedPermissions.contains(permission)) return;
        VoicePlayer player = event.getPlayer();

        Map<String, Boolean> permissions = Maps.newHashMap();
        permissions.put(permission, player.getInstance().hasPermission(permission));

        player.sendPacket(new ConfigPlayerInfoPacket(permissions));
    }

    @EventSubscribe(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        wrap(event.getPlayer());
    }

    @EventSubscribe(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerById.remove(event.getPlayerId());
    }
}
