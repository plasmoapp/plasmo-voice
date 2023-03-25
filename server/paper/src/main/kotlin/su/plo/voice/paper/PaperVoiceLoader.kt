package su.plo.voice.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperVoiceLoader : JavaPlugin() {

    private val voiceServer = PaperVoiceServer(this)

    override fun onEnable() {
        voiceServer.onInitialize()
    }

    override fun onDisable() {
        voiceServer.onShutdown()
    }
}
