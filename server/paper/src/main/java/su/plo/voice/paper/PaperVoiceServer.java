package su.plo.voice.paper;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.MinecraftServerLib;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.paper.PaperServerLib;
import su.plo.voice.paper.connection.PaperServerChannelHandler;
import su.plo.voice.paper.connection.PaperServerServiceChannelHandler;
import su.plo.voice.server.BaseVoiceServer;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.util.version.ModrinthLoader;

import java.io.File;

public final class PaperVoiceServer extends BaseVoiceServer implements Listener {

    private final JavaPlugin loader;
    private final PaperServerLib minecraftServerLib;

    private PaperServerChannelHandler handler;
    private PaperServerServiceChannelHandler serviceChannel;

    public PaperVoiceServer(@NotNull JavaPlugin loader) {
        super(ModrinthLoader.PAPER);

        this.loader = loader;
        this.minecraftServerLib = new PaperServerLib(loader, this::getLanguages);
    }

    public void onLoad() {
        registerDefaultCommandsAndPermissions();
        minecraftServerLib.getCommandManager().registerCommands(loader);
    }

    public void onInitialize() {
        loader.getServer().getPluginManager().registerEvents(this, loader);

        minecraftServerLib.setPermissions(createPermissionSupplier());
        super.onInitialize();

        this.handler = new PaperServerChannelHandler(this);
        eventBus.register(this, handler);
        loader.getServer().getPluginManager().registerEvents(handler, loader);
        loader.getServer().getMessenger().registerIncomingPluginChannel(loader, CHANNEL_STRING, handler);
        loader.getServer().getMessenger().registerOutgoingPluginChannel(loader, CHANNEL_STRING);

        this.serviceChannel = new PaperServerServiceChannelHandler(this);
        loader.getServer().getMessenger().registerIncomingPluginChannel(loader, SERVICE_CHANNEL_STRING, serviceChannel);
        loader.getServer().getMessenger().registerOutgoingPluginChannel(loader, SERVICE_CHANNEL_STRING);

        minecraftServerLib.getPlayers().forEach((player) -> playerManager.getPlayerById(player.getUUID())
                .ifPresent((voicePlayer) -> {
                    if (player.getRegisteredChannels().contains(CHANNEL_STRING)) {
                        tcpConnectionManager.requestPlayerInfo(voicePlayer);
                    }
                }));
    }

    public void onShutdown() {
        super.onShutdown();
        this.handler = null;
        this.serviceChannel = null;
    }

    @Override
    public @NotNull String getVersion() {
        return loader.getDescription().getVersion();
    }

    @Override
    public @NotNull File getConfigFolder() {
        return loader.getDataFolder();
    }

    @Override
    protected File modsFolder() {
        return new File("plugins");
    }

    @Override
    protected File addonsFolder() {
        return new File(modsFolder(), "PlasmoVoice/addons");
    }

    @Override
    public @NotNull MinecraftServerLib getMinecraftServer() {
        return minecraftServerLib;
    }

    @Override
    protected PermissionSupplier createPermissionSupplier() {
        return new PermissionSupplier() {
            @Override
            public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof Player))
                    throw new IllegalArgumentException("player is not " + Player.class);
                Player serverPlayer = (Player) player;

                PermissionDefault permissionDefault = minecraftServerLib.getPermissionsManager().getPermissionDefault(permission);

                return getPermission(serverPlayer, permission)
                        .booleanValue(permissionDefault.getValue(serverPlayer.isOp()));
            }

            @Override
            public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof Player))
                    throw new IllegalArgumentException("player is not " + Player.class);
                Player serverPlayer = (Player) player;

                if (!serverPlayer.isPermissionSet(permission)) return PermissionTristate.UNDEFINED;
                return serverPlayer.hasPermission(permission) ? PermissionTristate.TRUE : PermissionTristate.FALSE;
            }
        };
    }

    // Event handlers
    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        eventBus.call(new su.plo.voice.api.server.event.player.PlayerJoinEvent(player, player.getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        eventBus.call(new su.plo.voice.api.server.event.player.PlayerQuitEvent(player, player.getUniqueId()));
    }
}
