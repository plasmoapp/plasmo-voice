package su.plo.voice.server.audio.source

import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.source.StaticSourceInfo
import java.util.*

class VoiceServerStaticSource(
    voiceServer: PlasmoVoiceServer,
    addon: AddonContainer,
    line: ServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
    defaultPosition: ServerPos3d
) : VoiceServerPositionalSource<StaticSourceInfo>(voiceServer, addon, UUID.randomUUID(), line, decoderInfo, stereo),
    ServerStaticSource {

    private var sourcePosition: ServerPos3d = defaultPosition

    override var position: ServerPos3d
        get() = sourcePosition
        set(position) {
            if (this.sourcePosition != position) {
                this.sourcePosition = position
                setDirty()
            }
        }

    override val sourceInfo: StaticSourceInfo
        get() = StaticSourceInfo(
            addon.id,
            id,
            line.id,
            name,
            state.get().toByte(),
            decoderInfo,
            stereo,
            iconVisible,
            angle,
            position.toPosition(),
            position.lookAngle
        )
}
