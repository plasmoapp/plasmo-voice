package su.plo.voice.server.audio.line

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.proxy.PlasmoVoiceProxy
import su.plo.voice.api.server.audio.line.ProxySourceLine

class VoiceProxySourceLine(
    val voiceProxy: PlasmoVoiceProxy,
    override val addon: AddonContainer,
    name: String,
    translation: String,
    icon: String,
    defaultVolume: Double,
    weight: Int,
    withPlayers: Boolean
) : ProxySourceLine,
    VoiceBaseServerSourceLine(
        voiceProxy,
        addon,
        name,
        translation,
        icon,
        defaultVolume,
        weight,
        withPlayers
    )
