package su.plo.voice.api.server.audio.source

import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo

interface ServerSourceManager : BaseServerSourceManager {

    fun createPlayerSource(
        addonObject: Any,
        player: VoiceServerPlayer,
        line: ServerSourceLine,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerPlayerSource

    fun createEntitySource(
        addonObject: Any,
        entity: MinecraftServerEntity,
        line: ServerSourceLine,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerEntitySource

    fun createStaticSource(
        addonObject: Any,
        position: ServerPos3d,
        line: ServerSourceLine,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerStaticSource
}
