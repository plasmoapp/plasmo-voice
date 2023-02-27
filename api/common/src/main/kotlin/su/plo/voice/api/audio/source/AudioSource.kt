package su.plo.voice.api.audio.source

import su.plo.voice.proto.data.audio.source.SourceInfo

// TODO: doc
interface AudioSource<S : SourceInfo> {

    /**
     * @return audio source info
     */
    val sourceInfo: S
}
