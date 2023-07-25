package su.plo.voice.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ProxyReloadEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import org.bstats.bungeecord.Metrics
import su.plo.lib.api.proxy.event.command.ProxyCommandsRegisterEvent
import su.plo.lib.api.server.permission.PermissionTristate
import su.plo.lib.bungee.BungeeProxyLib
import su.plo.voice.bungee.connection.BungeeProxyChannelHandler
import su.plo.voice.proxy.BaseVoiceProxy
import su.plo.voice.server.player.PermissionSupplier
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

class BungeeVoiceProxy(
    private val plugin: Plugin,
    private val proxyServer: ProxyServer
) : BaseVoiceProxy(ModrinthLoader.BUNGEECORD) {

    private lateinit var minecraftServer: BungeeProxyLib
    
    private lateinit var metrics: Metrics

    fun onEnable() {
        minecraftServer = BungeeProxyLib(plugin, ::getLanguages)
        minecraftServer.permissions = createPermissionSupplier()

        // register commands
        ProxyCommandsRegisterEvent.invoker.onCommandsRegister(minecraftServer.commandManager, minecraftServer)
        minecraftServer.commandManager.registerCommands(plugin, proxyServer)
        proxyServer.pluginManager.registerListener(plugin, minecraftServer.commandManager)

        super.onInitialize()

        proxyServer.pluginManager.registerListener(plugin, minecraftServer)
        proxyServer.pluginManager.registerListener(plugin, BungeeProxyChannelHandler(this))

        this.metrics = Metrics(plugin, 18094)
    }

    fun onDisable() {
        super.onShutdown()
        metrics.shutdown()
    }

    @EventHandler
    fun onProxyConfigReload(event: ProxyReloadEvent) {
        super.onProxyConfigReload()
    }

    override fun getConfigFolder(): File = plugin.dataFolder

    override fun getConfigsFolder() = File("plugins")

    override fun getMinecraftServer() = minecraftServer

    override fun createPermissionSupplier() = object : PermissionSupplier {

        override fun hasPermission(player: Any, permission: String): Boolean {
            require(player is ProxiedPlayer) { "player is not ${ProxiedPlayer::class.java}" }

            val permissionDefault = minecraftServer.permissionsManager.getPermissionDefault(permission)

            return getPermission(player, permission)
                .booleanValue(permissionDefault.getValue(false))
        }

        override fun getPermission(player: Any, permission: String): PermissionTristate {
            require(player is ProxiedPlayer) { "player is not ${ProxiedPlayer::class.java}" }

            if (!player.permissions.contains(permission))
                return PermissionTristate.UNDEFINED

            return if (player.hasPermission(permission)) PermissionTristate.TRUE
            else PermissionTristate.FALSE
        }
    }
}
