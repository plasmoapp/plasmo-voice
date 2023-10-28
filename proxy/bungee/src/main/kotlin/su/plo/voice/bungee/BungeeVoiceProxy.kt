package su.plo.voice.bungee

import net.md_5.bungee.api.event.ProxyReloadEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import org.bstats.bungeecord.Metrics
import su.plo.slib.bungee.BungeeProxyLib
import su.plo.voice.proxy.BaseVoiceProxy
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

class BungeeVoiceProxy(
    private val plugin: Plugin
) : BaseVoiceProxy(ModrinthLoader.BUNGEECORD) {

    private lateinit var minecraftServer: BungeeProxyLib
    
    private lateinit var metrics: Metrics

    override fun getConfigFolder(): File = plugin.dataFolder

    override fun getMinecraftServer() = minecraftServer

    fun onEnable() {
        minecraftServer = BungeeProxyLib(plugin)

        super.onInitialize()

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
}
