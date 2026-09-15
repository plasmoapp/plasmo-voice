package su.plo.voice.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperVoiceLoader(
    private val voiceServer: PaperVoiceServer,
) : JavaPlugin() {
    init {
        voiceServer.plugin = this
    }

    override fun onEnable() {
        voiceServer.onInitialize()
    }

    override fun onDisable() {
        voiceServer.onShutdown()
    }
}
