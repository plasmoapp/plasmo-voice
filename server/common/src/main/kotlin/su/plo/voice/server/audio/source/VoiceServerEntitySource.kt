package su.plo.voice.server.audio.source


import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerEntitySource
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.source.EntitySourceInfo
import java.util.*

class VoiceServerEntitySource(
    voiceServer: PlasmoVoiceServer,
    addon: AddonContainer,
    line: ServerSourceLine,
    decoderInfo: CodecInfo?,
    stereo: Boolean,
    override val entity: McServerEntity
) : VoiceServerProximitySource<EntitySourceInfo>(voiceServer, addon, UUID.randomUUID(), line, decoderInfo, stereo),
    ServerEntitySource {

    override val position: ServerPos3d
        get() = entity.getServerPosition()

    override val sourceInfo: EntitySourceInfo
        get() = EntitySourceInfo(
            addon.id,
            id,
            line.id,
            name,
            state.get().toByte(),
            decoderInfo,
            stereo,
            iconVisible,
            angle,
            entity.id
        )
}
