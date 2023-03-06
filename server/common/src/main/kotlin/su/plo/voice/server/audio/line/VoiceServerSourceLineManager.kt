package su.plo.voice.server.audio.line

import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.line.ServerSourceLineManager

class VoiceServerSourceLineManager(
    private val voiceServer: PlasmoVoiceServer
) : ServerSourceLineManager,
    VoiceBaseServerSourceLineManager<ServerSourceLine>(voiceServer, voiceServer.tcpConnectionManager) {

    override fun register(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        weight: Int,
        withPlayers: Boolean
    ): ServerSourceLine {
        val addon = voiceServer.addonManager.getAddon(addonObject)
            .orElseThrow { IllegalArgumentException("addonObject is not an addon") }

        return VoiceServerSourceLine(
            voiceServer,
            addon,
            name,
            translation,
            icon,
            weight,
            withPlayers
        ).also { line ->
            line.playersSets?.let { voiceServer.eventBus.register(voiceServer, it) }
            lineById[line.id] = line
        }
    }
}
