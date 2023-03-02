package su.plo.voice.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.velocity.VelocityProxyLib;
import su.plo.voice.api.proxy.event.command.CommandsRegisterEvent;
import su.plo.voice.api.server.event.player.PlayerJoinEvent;
import su.plo.voice.api.server.event.player.PlayerQuitEvent;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.velocity.connection.VelocityProxyChannelHandler;

import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "plasmovoice",
        name = "PlasmoVoice",
        version = BuildConstants.VERSION
)
public final class VelocityVoiceProxy extends BaseVoiceProxy {

    private final ProxyServer proxyServer;
    private final Path dataDirectory;

    @Getter
    private final VelocityProxyLib minecraftServer;

    @Inject
    public VelocityVoiceProxy(@NotNull ProxyServer proxyServer,
                              @DataDirectory Path dataDirectory) {
        super(ModrinthLoader.VELOCITY);

        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.minecraftServer = new VelocityProxyLib(proxyServer, eventBus, this::getLanguages);
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        minecraftServer.setPermissions(createPermissionSupplier());

        // load addons before commands registration
        loadAddons();

        // register commands
        eventBus.call(new CommandsRegisterEvent(this, minecraftServer.getCommandManager()));
        minecraftServer.getCommandManager().registerCommands(proxyServer);
        proxyServer.getEventManager().register(this, minecraftServer.getCommandManager());

        super.onInitialize();

        proxyServer.getEventManager().register(this, new VelocityProxyChannelHandler(proxyServer, this));
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
        super.onShutdown();
    }

    // todo: move to listener?
    @Subscribe
    public void onPlayerJoin(@NotNull PostLoginEvent event) {
        eventBus.call(new PlayerJoinEvent(event.getPlayer(), event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void onPlayerQuit(@NotNull DisconnectEvent event) {
        eventBus.call(new PlayerQuitEvent(event.getPlayer(), event.getPlayer().getUniqueId()));
    }

    @Override
    public @NotNull String getVersion() {
        return BuildConstants.VERSION;
    }

    @Override
    public @NotNull File getConfigFolder() {
        return dataDirectory.toFile();
    }

    @Override
    protected File modsFolder() {
        return new File("plugins");
    }

    @Override
    protected PermissionSupplier createPermissionSupplier() {
        return new PermissionSupplier() {
            @Override
            public boolean hasPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof Player))
                    throw new IllegalArgumentException("player is not " + Player.class);
                Player serverPlayer = (Player) player;

                PermissionDefault permissionDefault = minecraftServer.getPermissionsManager().getPermissionDefault(permission);

                return getPermission(serverPlayer, permission)
                        .booleanValue(permissionDefault.getValue(false));
            }

            @Override
            public @NotNull PermissionTristate getPermission(@NotNull Object player, @NotNull String permission) {
                if (!(player instanceof Player))
                    throw new IllegalArgumentException("player is not " + Player.class);
                Player serverPlayer = (Player) player;

                if (serverPlayer.getPermissionValue(permission) == Tristate.UNDEFINED)
                    return PermissionTristate.UNDEFINED;

                return serverPlayer.hasPermission(permission) ? PermissionTristate.TRUE : PermissionTristate.FALSE;
            }
        };
    }
}
