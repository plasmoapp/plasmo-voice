package su.plo.lib.mod.server;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.entity.MinecraftServerEntity;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;
import su.plo.lib.api.server.event.player.PlayerQuitEvent;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.lib.api.server.world.MinecraftServerWorld;
import su.plo.lib.mod.chat.ComponentTextConverter;
import su.plo.lib.mod.client.texture.ResourceCache;
import su.plo.lib.mod.server.chat.ServerComponentTextConverter;
import su.plo.lib.mod.server.command.ModCommandManager;
import su.plo.lib.mod.server.entity.ModServerEntity;
import su.plo.lib.mod.server.entity.ModServerPlayer;
import su.plo.lib.mod.server.world.ModServerWorld;
import su.plo.voice.api.server.config.ServerLanguages;
import su.plo.voice.proto.data.player.MinecraftGameProfile;
import su.plo.voice.server.player.PermissionSupplier;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ModServerLib implements MinecraftServerLib {

    public static ModServerLib INSTANCE;

    private final Map<ServerLevel, MinecraftServerWorld> worldByInstance = Maps.newConcurrentMap();
    private final Map<UUID, MinecraftServerPlayerEntity> playerById = Maps.newConcurrentMap();

    @Setter
    private MinecraftServer server;
    @Setter
    private PermissionSupplier permissions;

    @Getter
    private final ServerComponentTextConverter textConverter;
    private final ResourceCache resources = new ResourceCache();

    @Getter
    private final ModCommandManager commandManager;
    @Getter
    private final PermissionsManager permissionsManager = new PermissionsManager();

    private ScheduledExecutorService backgroundExecutor;
    private ScheduledFuture<?> worldCleanupTask;

    public ModServerLib(@NotNull Supplier<ServerLanguages> languagesSupplier) {
        this.textConverter = new ServerComponentTextConverter(new ComponentTextConverter(), languagesSupplier);
        this.commandManager = new ModCommandManager(this, textConverter);

        PlayerQuitEvent.INSTANCE.registerListener(player -> playerById.remove(player.getUUID()));
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;

        this.backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
        this.worldCleanupTask = backgroundExecutor.scheduleAtFixedRate(
                this::worldsCleanupTick,
                0L,
                30L,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void onShutdown() {
        this.server = null;
        this.permissions = null;
        commandManager.clear();
        permissionsManager.clear();

        worldCleanupTask.cancel(false);
        backgroundExecutor.shutdown();
    }

    @Override
    public void executeInMainThread(@NotNull Runnable runnable) {
        server.execute(runnable);
    }

    @Override
    public @NotNull MinecraftServerWorld getWorld(@NotNull Object instance) {
        if (!(instance instanceof ServerLevel))
            throw new IllegalArgumentException("instance is not " + ServerLevel.class);

        return worldByInstance.computeIfAbsent(
                ((ServerLevel) instance),
                (level) -> new ModServerWorld(level.dimension().location(), level)
        );
    }

    @Override
    public Collection<MinecraftServerWorld> getWorlds() {
        return StreamSupport.stream(server.getAllLevels().spliterator(), false)
                .map(this::getWorld)
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull MinecraftServerPlayerEntity getPlayerByInstance(@NotNull Object instance) {
        if (!(instance instanceof ServerPlayer))
            throw new IllegalArgumentException("instance is not " + ServerPlayer.class);
        ServerPlayer serverInstance = (ServerPlayer) instance;

        MinecraftServerPlayerEntity serverPlayer = playerById.get(serverInstance.getUUID());
        if (serverPlayer == null) {
            serverPlayer = new ModServerPlayer(
                    this,
                    textConverter,
                    permissions,
                    resources,
                    serverInstance
            );
            playerById.put(serverInstance.getUUID(), serverPlayer);
        } else if (serverPlayer.getInstance() != serverInstance) {
            ((ModServerPlayer) serverPlayer).setInstance(serverInstance);
        }

        return serverPlayer;
    }

    @Override
    public Optional<MinecraftServerPlayerEntity> getPlayerByName(@NotNull String name) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(name);
        if (player == null) return Optional.empty();

        return Optional.of(getPlayerByInstance(player));
    }

    @Override
    public Optional<MinecraftServerPlayerEntity> getPlayerById(@NotNull UUID playerId) {
        MinecraftServerPlayerEntity serverPlayer = playerById.get(playerId);
        if (serverPlayer != null) return Optional.of(serverPlayer);

        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player == null) return Optional.empty();

        return Optional.of(getPlayerByInstance(player));
    }

    @Override
    public Optional<MinecraftGameProfile> getGameProfile(@NotNull UUID playerId) {
        //#if MC>=11701
        return server.getProfileCache().get(playerId)
                .map(this::getGameProfile);
        //#else
        //$$ return Optional.ofNullable(server.getProfileCache().get(playerId))
        //$$         .map(this::getGameProfile);
        //#endif
    }

    @Override
    public Optional<MinecraftGameProfile> getGameProfile(@NotNull String name) {
        //#if MC>=11701
        return server.getProfileCache().get(name)
                .map(this::getGameProfile);
        //#else
        //$$ return Optional.ofNullable(server.getProfileCache().get(name))
        //$$         .map(this::getGameProfile);
        //#endif
    }

    private MinecraftGameProfile getGameProfile(@NotNull GameProfile gameProfile) {
        return new MinecraftGameProfile(
                gameProfile.getId(),
                gameProfile.getName(),
                gameProfile.getProperties().values().stream()
                        .map((property) -> new MinecraftGameProfile.Property(
                                property.getName(),
                                property.getValue(),
                                property.getSignature()
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull Collection<MinecraftServerPlayerEntity> getPlayers() {
        return playerById.values();
    }

    @Override
    public @NotNull MinecraftServerEntity getEntity(@NotNull Object instance) {
        if (!(instance instanceof Entity))
            throw new IllegalArgumentException("instance is not " + Entity.class);

        return new ModServerEntity(
                this,
                ((Entity) instance)
        );
    }

    @Override
    public int getPort() {
        return server.getPort();
    }

    @Override
    public @NotNull String getVersion() {
        return server.getServerVersion();
    }

    private void worldsCleanupTick() {
        Set<ServerLevel> worlds = StreamSupport.stream(server.getAllLevels().spliterator(), false)
                .collect(Collectors.toSet());

        worldByInstance.keySet()
                .stream()
                .filter(world -> !worlds.contains(world))
                .forEach(worldByInstance::remove);
    }
}
