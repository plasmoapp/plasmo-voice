package su.plo.voice.api.server.audio.source

import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.proto.data.audio.source.StaticSourceInfo

/**
 * Represents a static audio source.
 */
interface ServerStaticSource : ServerPositionalSource<StaticSourceInfo> {

    /**
     * Gets or sets the position of this audio source.
     *
     * @return The position.
     */
    override var position: ServerPos3d
}
