package su.plo.voice.minestom

import net.minestom.server.extensions.Extension
class MinestomVoiceLoader : Extension() {

    private val voiceServer = MinestomVoiceServer(this)

    override fun initialize() {
        voiceServer.onInitialize()
    }

    override fun terminate() {
        voiceServer.onShutdown()
    }
}
