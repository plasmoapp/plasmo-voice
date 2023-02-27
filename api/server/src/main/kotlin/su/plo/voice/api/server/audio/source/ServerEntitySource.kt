package su.plo.voice.api.server.audio.source

import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.voice.proto.data.audio.source.EntitySourceInfo

interface ServerEntitySource : ServerPositionalSource<EntitySourceInfo> {

    val entity: MinecraftServerEntity
}
