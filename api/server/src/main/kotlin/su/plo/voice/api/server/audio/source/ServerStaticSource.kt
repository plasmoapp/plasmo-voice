package su.plo.voice.api.server.audio.source

import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.proto.data.audio.source.StaticSourceInfo

interface ServerStaticSource : ServerPositionalSource<StaticSourceInfo> {

    override var position: ServerPos3d
}
