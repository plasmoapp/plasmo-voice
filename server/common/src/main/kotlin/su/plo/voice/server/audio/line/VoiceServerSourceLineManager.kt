package su.plo.voice.server.audio.line

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.line.ServerSourceLineManager

class VoiceServerSourceLineManager(
    private val voiceServer: PlasmoVoiceServer
) : ServerSourceLineManager,
    VoiceBaseServerSourceLineManager<ServerSourceLine>(voiceServer, voiceServer.tcpConnectionManager) {

    override fun createSourceLine(
        addon: AddonContainer,
        name: String,
        translation: String,
        icon: String,
        weight: Int,
        withPlayers: Boolean,
        defaultVolume: Double
    ): ServerSourceLine {
        return VoiceServerSourceLine(
            voiceServer,
            addon,
            name,
            translation,
            icon,
            defaultVolume,
            voiceServer.config.voice().weights().getSourceLineWeight(name)
                .orElse(weight),
            withPlayers
        )
    }
}
