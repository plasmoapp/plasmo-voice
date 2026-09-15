@file:Suppress("UnstableApiUsage")

package su.plo.voice.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin
import su.plo.slib.paper.command.registerCommandHandler
import su.plo.voice.BaseVoice

class PaperVoiceBootstrap : PluginBootstrap {
    private val voiceServer = PaperVoiceServer()

    override fun bootstrap(context: BootstrapContext) {
        context.registerCommandHandler(BaseVoice.LOGGER)
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin =
        PaperVoiceLoader(voiceServer)
}
