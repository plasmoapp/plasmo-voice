package su.plo.voice.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.event.player.PlayerJoinEvent;
import su.plo.lib.api.server.event.player.PlayerQuitEvent;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.mod.server.ModServerLib;
import su.plo.ustats.UStats;
import su.plo.ustats.mod.ModUStatsPlatform;
import su.plo.voice.server.connection.ModServerChannelHandler;
import su.plo.voice.server.connection.ModServerServiceChannelHandler;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.util.version.ModrinthLoader;

//#if FABRIC
import me.lucko.fabric.api.permissions.v0.Permissions;
//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
//#else
//$$ import net.minecraftforge.event.RegisterCommandsEvent;
//$$ import net.minecraftforge.event.entity.player.PlayerEvent;
//$$ import net.minecraftforge.eventbus.api.SubscribeEvent;
//$$ import net.minecraftforge.server.permission.PermissionAPI;
//$$ import java.util.Optional;

//#if MC>=11802
//$$ import net.minecraftforge.event.server.ServerStartedEvent;
//$$ import net.minecraftforge.event.server.ServerStoppingEvent;
//$$ import net.minecraftforge.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.server.permission.nodes.PermissionNode;
//#elseif MC>=11701
//$$ import net.minecraftforge.fmllegacy.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.fmlserverevents.FMLServerStartedEvent;
//$$ import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
//#else
//$$ import net.minecraftforge.fml.network.event.EventNetworkChannel;
//$$ import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
//$$ import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
//#endif

//#endif

import java.io.File;

public final class ModVoiceServer
        extends BaseVoiceServer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);
    public static final ResourceLocation FLAG_CHANNEL = new ResourceLocation(FLAG_CHANNEL_STRING);
    public static final ResourceLocation SERVICE_CHANNEL = new ResourceLocation(SERVICE_CHANNEL_STRING);

    private final String modId = "plasmovoice";

    private final ModServerLib minecraftServerLib = new ModServerLib(this::getLanguages);

    private MinecraftServer server;
    private ModServerChannelHandler handler;
    private ModServerServiceChannelHandler serviceHandler;

    private UStats uStats;

    //#if FORGE
    //$$ private final EventNetworkChannel channel;
    //$$ private final EventNetworkChannel serviceChannel;
    //$$
    //$$ public ModVoiceServer(@NotNull ModrinthLoader loader, @NotNull EventNetworkChannel channel, @NotNull EventNetworkChannel serviceChannel) {
    //$$     super(loader);
    //$$     this.channel = channel;
    //$$     this.serviceChannel = serviceChannel;
    //$$ }
    //#else
    public ModVoiceServer(@NotNull ModrinthLoader loader) {
        super(loader);
    }
    //#endif


    private void onInitialize(MinecraftServer server) {
        this.server = server;
        minecraftServerLib.setServer(server);
        minecraftServerLib.setPermissions(createPermissionSupplier());
        minecraftServerLib.onInitialize();
        super.onInitialize();

        this.uStats = new UStats(
                USTATS_PROJECT_UUID,
                getVersion(),
                new ModUStatsPlatform(server),
                getConfigFolder()
        );
    }

    private void onShutdown(MinecraftServer server) {
        if (uStats != null) uStats.close();

        super.onShutdown();
        this.server = null;
        minecraftServerLib.onShutdown();
        handler.clear();
    }

    private void onCommandRegister(@NotNull CommandDispatcher<CommandSourceStack> dispatcher) {
        registerDefaultCommandsAndPermissions();
        minecraftServerLib.getCommandManager().registerCommands(dispatcher);
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId + "/server");
    }

    @Override
    public @NotNull File getConfigsFolder() {
        return new File("config");
    }

    @Override
    public @NotNull MinecraftServerLib getMinecraftServer() {
        return minecraftServerLib;
    }

    //#if FABRIC
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            if (handler == null) {
                this.handler = new ModServerChannelHandler(this);
                S2CPlayChannelEvents.REGISTER.register(handler);
                ServerPlayNetworking.registerGlobalReceiver(CHANNEL, handler);
            }

            if (serviceHandler == null) {
                this.serviceHandler = new ModServerServiceChannelHandler(this);
                ServerPlayNetworking.registerGlobalReceiver(SERVICE_CHANNEL, serviceHandler);
            }

            this.onInitialize(server);
            eventBus.register(this, handler);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onShutdown);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, mcServer) ->
                PlayerJoinEvent.INSTANCE.getInvoker().onPlayerJoin(
                        minecraftServerLib.getPlayerByInstance(handler.player)
                )
        );
        ServerPlayConnectionEvents.DISCONNECT.register((handler, mcServer) ->
                PlayerQuitEvent.INSTANCE.getInvoker().onPlayerQuit(
                        minecraftServerLib.getPlayerByInstance(handler.player)
                )
        );
        //#if MC>=11900
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, selection) -> onCommandRegister(dispatcher));
        //#else
        //$$ CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> onCommandRegister(dispatcher));
        //#endif
    }

    @Override
    protected PermissionSupplier createPermissionSupplier() {
        return new PermissionSupplier() {
            @Override
            public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);
                ServerPlayer serverPlayer = (ServerPlayer) player;

                PermissionDefault permissionDefault = minecraftServerLib.getPermissionsManager().getPermissionDefault(permission);
                boolean isOp = server.getPlayerList().isOp(serverPlayer.getGameProfile());

                return getPermission(serverPlayer, permission).booleanValue(permissionDefault.getValue(isOp));
            }

            @Override
            public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof ServerPlayer))
                    throw new IllegalArgumentException("player is not " + ServerPlayer.class);
                ServerPlayer serverPlayer = (ServerPlayer) player;

                return toPermissionTristate(Permissions.getPermissionValue(serverPlayer, permission));
            }

            private PermissionTristate toPermissionTristate(TriState triState) {
                switch (triState) {
                    case TRUE: return PermissionTristate.TRUE;
                    case FALSE: return PermissionTristate.FALSE;
                    default: return PermissionTristate.UNDEFINED;
                }
            }
        };
    }
    //#else
    //$$ @Override
    //$$ protected PermissionSupplier createPermissionSupplier() {
    //$$     return new PermissionSupplier() {
    //$$         @Override
    //$$         public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
    //$$             if (!(player instanceof ServerPlayer))
    //$$                 throw new IllegalArgumentException("player is not " + ServerPlayer.class);
    //$$             ServerPlayer serverPlayer = (ServerPlayer) player;
    //$$
    //$$             PermissionDefault permissionDefault = minecraftServerLib.getPermissionsManager().getPermissionDefault(permission);
    //$$             boolean isOp = server.getPlayerList().isOp(serverPlayer.getGameProfile());
    //$$
    //$$             return getPermission(serverPlayer, permission).booleanValue(permissionDefault.getValue(isOp));
    //$$         }
    //$$
    //$$         @Override
    //$$         public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
    //$$             if (!(player instanceof ServerPlayer))
    //$$                 throw new IllegalArgumentException("player is not " + ServerPlayer.class);
    //$$             ServerPlayer serverPlayer = (ServerPlayer) player;
    //$$
                     //#if MC>=11802
                     //$$ Optional<PermissionNode<?>> permissionNode = PermissionAPI.getRegisteredNodes().stream()
                     //$$         .filter((node) -> node.getNodeName().equals(permission))
                     //$$         .findAny();
                     //$$
                     //$$ if (permissionNode.isEmpty()) return PermissionTristate.UNDEFINED;
                     //$$
                     //$$ Boolean value = (Boolean) permissionNode.get().getDefaultResolver().resolve(serverPlayer, serverPlayer.getUUID());
                     //$$ if (value == null) return PermissionTristate.UNDEFINED;
                     //$$
                     //$$ return value ? PermissionTristate.TRUE : PermissionTristate.FALSE;
                     //#else
                     //$$ if (!PermissionAPI.getPermissionHandler().getRegisteredNodes().contains(permission))
                     //$$     return PermissionTristate.UNDEFINED;
                     //$$
                     //$$ return PermissionAPI.hasPermission(serverPlayer, permission)
                     //$$         ? PermissionTristate.TRUE
                     //$$         : PermissionTristate.FALSE;
                     //#endif
    //$$         }
    //$$     };
    //$$ }
    //$$ @SubscribeEvent
    //#if MC>=11802
    //$$ public void onServerStart(ServerStartedEvent event) {
    //#else
    //$$ public void onServerStart(FMLServerStartedEvent event) {
    //#endif
    //$$     if (handler == null) {
    //$$         this.handler = new ModServerChannelHandler(this);
    //$$         channel.addListener(handler::receive);
    //$$     }
    //$$
    //$$     if (serviceHandler == null) {
    //$$         this.serviceHandler = new ModServerServiceChannelHandler(this);
    //$$         serviceChannel.addListener(serviceHandler::receive);
    //$$     }
    //$$
    //$$     onInitialize(event.getServer());
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //#if MC>=11802
    //$$ public void onServerStopping(ServerStoppingEvent event) {
    //#else
    //$$ public void onServerStopping(FMLServerStoppingEvent event) {
    //#endif
    //$$     onShutdown(event.getServer());
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
    //$$     if (event.getEntity() instanceof ServerPlayer) {
    //$$         PlayerJoinEvent.INSTANCE.getInvoker().onPlayerJoin(
    //$$             minecraftServerLib.getPlayerByInstance(event.getEntity())
    //$$         );
    //$$     }
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
    //$$     if (event.getEntity() instanceof ServerPlayer) {
    //$$         PlayerQuitEvent.INSTANCE.getInvoker().onPlayerQuit(
    //$$             minecraftServerLib.getPlayerByInstance(event.getEntity())
    //$$         );
    //$$     }
    //$$ }
    //$$
    //$$ @SubscribeEvent
    //$$ public void onCommandRegister(RegisterCommandsEvent event) {
    //$$     onCommandRegister(event.getDispatcher());
    //$$ }
    //#endif
}
