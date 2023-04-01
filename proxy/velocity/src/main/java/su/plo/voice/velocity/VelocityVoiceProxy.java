package su.plo.voice.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.event.command.ProxyCommandsRegisterEvent;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionTristate;
import su.plo.lib.velocity.VelocityProxyLib;
import su.plo.voice.proxy.BaseVoiceProxy;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.velocity.connection.VelocityProxyChannelHandler;

import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "plasmovoice",
        name = "PlasmoVoice",
        version = BuildConstants.VERSION,
        authors = "Apehum",
        dependencies = {
                @Dependency(id = "luckperms", optional = true)
        }
)
public final class VelocityVoiceProxy extends BaseVoiceProxy {

    private final ProxyServer proxyServer;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    @Getter
    private final VelocityProxyLib minecraftServer;
    
    private Metrics metrics;

    @Inject
    public VelocityVoiceProxy(@NotNull ProxyServer proxyServer,
                              @DataDirectory Path dataDirectory,
                              @NotNull Metrics.Factory metricsFactory) {
        super(ModrinthLoader.VELOCITY);

        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;

        this.minecraftServer = new VelocityProxyLib(proxyServer, eventBus, this::getLanguages);
    }

    @Subscribe
    public void onProxyInitialization(@NotNull ProxyInitializeEvent event) {
        minecraftServer.setPermissions(createPermissionSupplier());

        // register commands
        ProxyCommandsRegisterEvent.INSTANCE.getInvoker().onCommandsRegister(minecraftServer.getCommandManager(), minecraftServer);
        minecraftServer.getCommandManager().registerCommands(proxyServer);
        proxyServer.getEventManager().register(this, minecraftServer.getCommandManager());

        super.onInitialize();

        proxyServer.getEventManager().register(this, minecraftServer);
        proxyServer.getEventManager().register(this, new VelocityProxyChannelHandler(proxyServer, this));

        this.metrics = metricsFactory.make(this, 18095);
    }

    @Subscribe
    public void onProxyShutdown(@NotNull ProxyShutdownEvent event) {
        super.onShutdown();

        metrics.shutdown();
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
    public @NotNull File getConfigsFolder() {
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
