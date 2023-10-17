package su.plo.voice.paper

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.spigot.SpigotServerLib
import su.plo.ustats.UStats
import su.plo.ustats.paper.PaperUStatsPlatform
import su.plo.voice.paper.integration.VoicePlaceholder
import su.plo.voice.server.BaseVoiceServer
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

class PaperVoiceServer(
    private val plugin: JavaPlugin
) : BaseVoiceServer(ModrinthLoader.PAPER), Listener {

    private val minecraftServerLib = SpigotServerLib(plugin)

    private lateinit var uStats: UStats

    public override fun onInitialize() {
        minecraftServerLib.onInitialize()

        super.onInitialize()

        minecraftServerLib.players.forEach { player ->
            playerManager.getPlayerById(player.uuid)
                .ifPresent { voicePlayer ->
                    if (player.registeredChannels.contains(CHANNEL_STRING)) {
                        tcpPacketManager.requestPlayerInfo(voicePlayer)
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
        minecraftServerLib.onShutdown()
    }

    override fun getConfigFolder() = plugin.dataFolder

    override fun getConfigsFolder() = File("plugins")

    override fun getMinecraftServer() = minecraftServerLib
}
