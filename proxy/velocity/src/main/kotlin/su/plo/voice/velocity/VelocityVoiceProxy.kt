package su.plo.voice.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import org.bstats.velocity.Metrics
import su.plo.slib.api.proxy.McProxyLib
import su.plo.slib.velocity.VelocityProxyLib
import su.plo.voice.BuildConstants
import su.plo.voice.proxy.BaseVoiceProxy
import su.plo.voice.util.version.ModrinthLoader
import su.plo.voice.velocity.channel.ForgeRegisterChannelListener
import java.io.File
import java.nio.file.Path

@Plugin(
    id = "plasmovoice",
    name = "PlasmoVoice",
    version = BuildConstants.VERSION,
    authors = ["Apehum"],
    dependencies = [Dependency(id = "luckperms", optional = true)]
)
class VelocityVoiceProxy @Inject constructor(
    private val proxyServer: ProxyServer,
    @DataDirectory private val dataDirectory: Path,
    private val metricsFactory: Metrics.Factory
) : BaseVoiceProxy(ModrinthLoader.VELOCITY) {

    private lateinit var minecraftProxy: McProxyLib

    private lateinit var metrics: Metrics

    override fun getConfigFolder(): File =
        dataDirectory.toFile()

    override fun getMinecraftServer() =
        minecraftProxy

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        minecraftProxy = VelocityProxyLib(proxyServer, this)

        super.onInitialize()

        metrics = metricsFactory.make(this, 18095)

        proxyServer.eventManager.register(this, ForgeRegisterChannelListener())
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        super.onShutdown()
        metrics.shutdown()
    }

    @Subscribe
    fun onProxyConfigReload(event: ProxyReloadEvent) {
        super.onProxyConfigReload()
    }
}
