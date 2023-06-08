package su.plo.voice.paper

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import su.plo.lib.api.server.permission.PermissionTristate
import su.plo.lib.paper.PaperServerLib
import su.plo.ustats.UStats
import su.plo.ustats.paper.PaperUStatsPlatform
import su.plo.voice.paper.connection.PaperServerChannelHandler
import su.plo.voice.paper.connection.PaperServerServiceChannelHandler
import su.plo.voice.paper.integration.VoicePlaceholder
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.server.player.PermissionSupplier
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

class PaperVoiceServer(
    private val plugin: JavaPlugin
) : BaseVoiceServer(ModrinthLoader.PAPER), Listener {

    private val minecraftServerLib = PaperServerLib(plugin, ::languages)

    private lateinit var handler: PaperServerChannelHandler
    private lateinit var serviceHandler: PaperServerServiceChannelHandler

    private lateinit var uStats: UStats

    public override fun onInitialize() {
        registerDefaultCommandsAndPermissions()
        minecraftServerLib.commandManager.registerCommands(plugin)

        plugin.server.pluginManager.registerEvents(minecraftServerLib, plugin)

        minecraftServerLib.permissions = createPermissionSupplier()
        super.onInitialize()

        handler = PaperServerChannelHandler(this)
        eventBus.register(this, handler)
        plugin.server.pluginManager.registerEvents(handler, plugin)
        plugin.server.messenger.registerIncomingPluginChannel(plugin, CHANNEL_STRING, handler)
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, CHANNEL_STRING)

        serviceHandler = PaperServerServiceChannelHandler(this)
        plugin.server.messenger.registerIncomingPluginChannel(plugin, SERVICE_CHANNEL_STRING, serviceHandler)
        plugin.server.messenger.registerOutgoingPluginChannel(plugin, SERVICE_CHANNEL_STRING)

        minecraftServerLib.players.forEach { player ->
            playerManager.getPlayerById(player.uuid)
                .ifPresent { voicePlayer ->
                    if (player.registeredChannels.contains(CHANNEL_STRING)) {
                        tcpConnectionManager.requestPlayerInfo(voicePlayer)
                    }
                }
        }

        this.uStats = UStats(
            USTATS_PROJECT_UUID,
            version,
            PaperUStatsPlatform,
            configFolder
        )

        // Initialize integrations
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            VoicePlaceholder(this).register()
        }
    }

    public override fun onShutdown() {
        uStats.close()
        super.onShutdown()
    }

    override fun getVersion() = plugin.description.version

    override fun getConfigFolder() = plugin.dataFolder

    override fun getConfigsFolder() = File("plugins")

    override fun getMinecraftServer() = minecraftServerLib

    override fun createPermissionSupplier(): PermissionSupplier {
        return object : PermissionSupplier {

            override fun hasPermission(player: Any, permission: String): Boolean {
                require(player is Player) { "player is not ${Player::class.java}" }

                val permissionDefault = minecraftServerLib.permissionsManager.getPermissionDefault(permission)

                return getPermission(player, permission)
                    .booleanValue(permissionDefault.getValue(player.isOp))
            }

            override fun getPermission(player: Any, permission: String): PermissionTristate {
                require(player is Player) { "player is not ${Player::class.java}" }

                if (!player.isPermissionSet(permission)) return PermissionTristate.UNDEFINED

                return if (player.hasPermission(permission)) PermissionTristate.TRUE else PermissionTristate.FALSE
            }
        }
    }
}
