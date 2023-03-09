package su.plo.voice.server.audio.line

import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.server.audio.line.ProxySourceLine
import su.plo.voice.api.server.audio.line.ProxySourceLineManager

class VoiceProxySourceLineManager(
    private val voiceProxy: PlasmoVoiceProxy
) : ProxySourceLineManager,
    VoiceBaseServerSourceLineManager<ProxySourceLine>(voiceProxy, voiceProxy.playerManager) {

    override fun register(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        weight: Int,
        withPlayers: Boolean,
        defaultVolume: Double
    ): ProxySourceLine {
        val addon = voiceProxy.addonManager.getAddon(addonObject)
            .orElseThrow { IllegalArgumentException("addonObject is not an addon") }

        return VoiceProxySourceLine(
            voiceProxy,
            addon,
            name,
            translation,
            icon,
            defaultVolume,
            weight,
            withPlayers
        ).also { line ->
            line.playersSets?.let { voiceProxy.eventBus.register(voiceProxy, it) }
            lineById[line.id] = line
        }
    }
}
