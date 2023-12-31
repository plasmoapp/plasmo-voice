package su.plo.lib.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import su.plo.lib.api.proxy.MinecraftProxyLib
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer
import su.plo.lib.api.proxy.server.MinecraftProxyServerInfo
import su.plo.lib.api.server.event.player.PlayerJoinEvent
import su.plo.lib.api.server.event.player.PlayerQuitEvent
import su.plo.lib.api.server.permission.PermissionsManager
import su.plo.lib.bungee.chat.BaseComponentTextConverter
import su.plo.lib.bungee.command.BungeeCommandManager
import su.plo.lib.bungee.player.BungeeProxyPlayer
import su.plo.lib.bungee.server.BungeeProxyServerInfo
import su.plo.voice.api.server.config.ServerLanguages
import su.plo.voice.proxy.event.player.McProxyServerConnectedEvent
import su.plo.voice.server.player.PermissionSupplier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer
import java.util.function.Supplier

class BungeeProxyLib(
    loader: Plugin,
    languagesSupplier: Supplier<ServerLanguages?>
) : MinecraftProxyLib, Listener {

    private val proxyServer = ProxyServer.getInstance()

    private val playerById: MutableMap<UUID, BungeeProxyPlayer> = ConcurrentHashMap()
    private val serverByName: MutableMap<String, BungeeProxyServerInfo> = ConcurrentHashMap()

    private val textConverter = BaseComponentTextConverter(languagesSupplier)
    private val commandManager = BungeeCommandManager(this, textConverter)
    private val permissionsManager = PermissionsManager()

    lateinit var permissions: PermissionSupplier

    init {
        proxyServer.pluginManager.registerListener(loader, commandManager)
        loadServers()
    }

    override fun getTextConverter() = textConverter

    override fun getCommandManager() = commandManager

    override fun getPermissionsManager() = permissionsManager

    override fun getPlayerById(playerId: UUID): Optional<MinecraftProxyPlayer> =
        proxyServer.getPlayer(playerId)?.let {
            Optional.of(getPlayerByInstance(it))
        } ?: Optional.empty()

    override fun getPlayerByName(name: String): Optional<MinecraftProxyPlayer> =
        proxyServer.getPlayer(name)?.let {
            Optional.of(getPlayerByInstance(it))
        } ?: Optional.empty()

    override fun getPlayerByInstance(instance: Any): MinecraftProxyPlayer {
        require(instance is ProxiedPlayer) { "instance is not ${ProxiedPlayer::class.java}" }

        return playerById.getOrPut(instance.uniqueId) {
            BungeeProxyPlayer(this, textConverter, permissions, instance)
        }
    }

    override fun getPlayers(): Collection<MinecraftProxyPlayer> =
        playerById.values

    override fun getServerByName(name: String): Optional<MinecraftProxyServerInfo> {
        serverByName[name]?.let { serverInfo ->
            val server = proxyServer.getServerInfo(name) ?: run {
                serverByName.remove(name)
                return Optional.empty()
            }

            if (server != serverInfo.instance) {
                return Optional.of(BungeeProxyServerInfo(server).also {
                    serverByName[name] = it
                })
            }

            return Optional.of(serverInfo)
        }

        return proxyServer.getServerInfo(name)?.let {
            Optional.of(getServerInfoByServerInstance(it))
        } ?: Optional.empty()
    }

    override fun getServerInfoByServerInstance(instance: Any): MinecraftProxyServerInfo {
        require(instance is ServerInfo) { "instance is not ${ServerInfo::class.java}" }

        serverByName[instance.name]?.let {
            if (it.instance == instance) return it
        }

        return BungeeProxyServerInfo(instance).also {
            serverByName[instance.name] = it
        }
    }

    override fun getServers(): Collection<MinecraftProxyServerInfo> =
        proxyServer.servers.values.map { getServerInfoByServerInstance(it) }

    override fun getPort(): Int =
        proxyServer.config.listeners.first().host.port

    private fun loadServers() {
        proxyServer.servers.values.forEach { serverInfo ->
            serverByName[serverInfo.name] = BungeeProxyServerInfo(serverInfo)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PostLoginEvent) {
        PlayerJoinEvent.invoker.onPlayerJoin(
            getPlayerByInstance(event.player)
        )
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerDisconnectEvent) {
        PlayerQuitEvent.invoker.onPlayerQuit(
            getPlayerByInstance(event.player)
        )
        playerById.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onServerSwitch(event: ServerSwitchEvent) {
        val player = getPlayerByInstance(event.player)
        val previousServer = event.from?.let { getServerInfoByServerInstance(it) }

        McProxyServerConnectedEvent.invoker.onServerConnected(player, previousServer)
    }
}
