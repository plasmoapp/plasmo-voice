package su.plo.voice.api.server.audio.source

import su.plo.voice.api.audio.source.AudioSourceManager
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo
import java.util.*

interface BaseServerSourceManager : AudioSourceManager<ServerAudioSource<*>> {

    fun createDirectSource(
        addonObject: Any,
        line: ServerSourceLine,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerDirectSource

    fun remove(sourceId: UUID)

    fun remove(source: ServerAudioSource<*>)
}
