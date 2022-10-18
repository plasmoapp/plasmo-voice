package su.plo.lib.paper;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.profile.MinecraftGameProfile;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.entity.MinecraftServerPlayer;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.paper.chat.BaseComponentTextConverter;
import su.plo.lib.paper.command.PaperCommandManager;
import su.plo.lib.paper.entity.PaperServerEntity;
import su.plo.lib.paper.entity.PaperServerPlayer;
import su.plo.lib.paper.world.PaperServerWorld;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;
import su.plo.voice.server.player.PermissionSupplier;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public final class PaperServerLib implements MinecraftServerLib {

    private final JavaPlugin loader;

    @Setter
    private PermissionSupplier permissions;

    private final Map<World, MinecraftServerWorld> worldByInstance = Maps.newConcurrentMap();
    private final Map<UUID, MinecraftServerPlayer> playerById = Maps.newConcurrentMap();

    private final BaseComponentTextConverter textConverter = new BaseComponentTextConverter();

    @Getter
    private final PaperCommandManager commandManager = new PaperCommandManager(this, textConverter);
    @Getter
    private final PermissionsManager permissionsManager = new PermissionsManager();

    @Override
    public void onShutdown() {
        this.permissions = null;
        commandManager.clear();
        permissionsManager.clear();
    }

    @Override
    public void executeInMainThread(@NotNull Runnable runnable) {
        Bukkit.getServer().getScheduler().runTask(loader, runnable);
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld(@NotNull Object instance) {
        if (!(instance instanceof World))
            throw new IllegalArgumentException("instance is not " + World.class);

        return worldByInstance.computeIfAbsent(((World) instance), PaperServerWorld::new);
    }

    @Override
    public @NotNull MinecraftServerPlayer getPlayerByInstance(@NotNull Object instance) {
        if (!(instance instanceof Player))
            throw new IllegalArgumentException("instance is not " + Player.class);
        Player serverPlayer = (Player) instance;

        return playerById.computeIfAbsent(
                serverPlayer.getUniqueId(),
                (playerId) -> new PaperServerPlayer(
                        loader,
                        this,
                        textConverter,
                        permissions,
                        serverPlayer
                )
        );
    }

    @Override
    public Optional<MinecraftServerPlayer> getPlayerByName(@NotNull String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null) return Optional.empty();

        return Optional.of(getPlayerByInstance(player));
    }

    @Override
    public Optional<MinecraftServerPlayer> getPlayerById(@NotNull UUID playerId) {
        MinecraftServerPlayer serverPlayer = playerById.get(playerId);
        if (serverPlayer != null) return Optional.of(serverPlayer);

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return Optional.empty();

        return Optional.of(getPlayerByInstance(player));
    }

    @Override
    public Optional<MinecraftGameProfile> getGameProfile(@NotNull UUID playerId) {
        return Optional.of(Bukkit.getServer().getOfflinePlayer(playerId))
                .filter(OfflinePlayer::hasPlayedBefore)
                .map(offlinePlayer -> new MinecraftGameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
    }

    @Override
    public Optional<MinecraftGameProfile> getGameProfile(@NotNull String name) {
        return Optional.of(Bukkit.getServer().getOfflinePlayer(name))
                .filter(OfflinePlayer::hasPlayedBefore)
                .map(offlinePlayer -> new MinecraftGameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
    }

    @Override
    public @NotNull Collection<MinecraftServerPlayer> getPlayers() {
        return playerById.values();
    }

    @Override
    public @NotNull MinecraftServerEntity getEntity(@NotNull Object instance) {
        if (!(instance instanceof Entity))
            throw new IllegalArgumentException("instance is not " + Entity.class);

        return new PaperServerEntity<>(
                this,
                ((Entity) instance)
        );
    }

    @EventSubscribe
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        getPlayerByInstance(event.getPlayer());
    }

    @EventSubscribe
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playerById.remove(event.getPlayerId());
    }
}
