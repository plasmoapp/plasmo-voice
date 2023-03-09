package su.plo.voice.server.audio.line

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.server.audio.line.ProxySourceLine
import su.plo.voice.api.server.audio.line.ProxySourceLineManager

class VoiceProxySourceLineManager(
    private val voiceProxy: PlasmoVoiceProxy
) : ProxySourceLineManager,
    VoiceBaseServerSourceLineManager<ProxySourceLine>(voiceProxy, voiceProxy.playerManager) {

    override fun createSourceLine(
        addon: AddonContainer,
        name: String,
        translation: String,
        icon: String,
        weight: Int,
        withPlayers: Boolean,
        defaultVolume: Double
    ): ProxySourceLine {
        return VoiceProxySourceLine(
            voiceProxy,
            addon,
            name,
            translation,
            icon,
            defaultVolume,
            weight,
            withPlayers
        )
    }
}
