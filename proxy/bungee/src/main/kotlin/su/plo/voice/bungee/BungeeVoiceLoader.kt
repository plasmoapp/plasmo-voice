package su.plo.voice.bungee

import net.md_5.bungee.api.plugin.Plugin

class BungeeVoiceLoader : Plugin() {

    private val voiceProxy = BungeeVoiceProxy(this)

    override fun onEnable() {
        voiceProxy.onEnable()
    }

    override fun onDisable() {
        voiceProxy.onDisable()
    }
}
