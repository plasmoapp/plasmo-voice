@file:Suppress("UnstableApiUsage")

package su.plo.voice.paper

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import su.plo.slib.paper.command.registerCommandHandler
import su.plo.voice.BaseVoice

class PaperVoiceBootstrap : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        context.registerCommandHandler(BaseVoice.LOGGER)
    }
}
