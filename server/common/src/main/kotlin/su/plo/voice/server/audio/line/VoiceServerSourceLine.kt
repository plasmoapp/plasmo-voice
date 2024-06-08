package su.plo.voice.server.audio.line

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerEntitySource
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.server.audio.source.VoiceServerEntitySource
import su.plo.voice.server.audio.source.VoiceServerPlayerSource
import su.plo.voice.server.audio.source.VoiceServerStaticSource

class VoiceServerSourceLine(
    override val voiceServer: PlasmoVoiceServer,
    override val addon: AddonContainer,
    name: String,
    translation: String,
    icon: String,
    defaultVolume: Double,
    weight: Int,
    withPlayers: Boolean
) : ServerSourceLine,
    VoiceBaseServerSourceLine(
        voiceServer,
        addon,
        name,
        translation,
        icon,
        defaultVolume,
        weight,
        withPlayers
    ) {

    override fun createPlayerSource(
        player: VoiceServerPlayer,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerPlayerSource =
        VoiceServerPlayerSource(
            voiceServer,
            addon,
            this,
            decoderInfo,
            stereo,
            player
        ).also(::addSource)

    override fun createEntitySource(
        entity: McServerEntity,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerEntitySource =
        VoiceServerEntitySource(
            voiceServer,
            addon,
            this,
            decoderInfo,
            stereo,
            entity
        ).also(::addSource)

    override fun createStaticSource(
        position: ServerPos3d,
        stereo: Boolean,
        decoderInfo: CodecInfo?
    ): ServerStaticSource =
        VoiceServerStaticSource(
            voiceServer,
            addon,
            this,
            decoderInfo,
            stereo,
            position
        ).also(::addSource)
}
