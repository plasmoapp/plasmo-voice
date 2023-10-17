package su.plo.voice.api.audio.source

import su.plo.voice.proto.data.audio.source.SourceInfo

/**
 * Base interface for audio sources.
 */
interface AudioSource<S : SourceInfo> {

    /**
     * Retrieves the source information.
     *
     * @return The source information.
     */
    val sourceInfo: S
}
