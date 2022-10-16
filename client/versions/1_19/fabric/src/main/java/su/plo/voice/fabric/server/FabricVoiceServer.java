package su.plo.voice.fabric.server;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.voice.fabric.server.connection.FabricServerChannelHandler;
import su.plo.voice.mod.server.ModVoiceServer;
import su.plo.voice.server.event.player.PlayerJoinEvent;
import su.plo.voice.server.event.player.PlayerQuitEvent;
import su.plo.voice.server.player.PermissionSupplier;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FabricVoiceServer extends ModVoiceServer<FabricServerChannelHandler> implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            this.onInitialize(server);
            eventBus.register(this, handler);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onShutdown);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, mcServer) ->
                eventBus.call(new PlayerJoinEvent(handler.getPlayer(), handler.getPlayer().getUUID()))
        );
        ServerPlayConnectionEvents.DISCONNECT.register((handler, mcServer) ->
                eventBus.call(new PlayerQuitEvent(handler.getPlayer(), handler.getPlayer().getUUID()))
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, selection) ->
                onCommandRegister(dispatcher)
        );

        this.handler = createChannelHandler();
        S2CPlayChannelEvents.REGISTER.register(handler);
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, handler);
    }

    @Override
    protected void onShutdown(MinecraftServer server) {
        super.onShutdown(server);
    }

    @Override
    protected FabricServerChannelHandler createChannelHandler() {
        return new FabricServerChannelHandler(this);
    }

    @Override
    public @NotNull String getVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer(modId)
                .orElse(null);
        checkNotNull(modContainer, "modContainer cannot be null");
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    protected PermissionSupplier createPermissionSupplier() {
        return new PermissionSupplier() {
            @Override
            public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer serverPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);

                PermissionDefault permissionDefault = minecraftServerLib.getPermissionsManager().getPermissionDefault(permission);
                boolean isOp = server.getPlayerList().isOp(serverPlayer.getGameProfile());

                return getPermission(serverPlayer, permission).booleanValue(permissionDefault.getValue(isOp));
            }

            @Override
            public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer serverPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);

                return toPermissionTristate(Permissions.getPermissionValue(serverPlayer, permission));
            }

            private PermissionTristate toPermissionTristate(TriState triState) {
                return switch (triState) {
                    case TRUE -> PermissionTristate.TRUE;
                    case FALSE -> PermissionTristate.FALSE;
                    case DEFAULT -> PermissionTristate.UNDEFINED;
                };
            }
        };
    }
}
