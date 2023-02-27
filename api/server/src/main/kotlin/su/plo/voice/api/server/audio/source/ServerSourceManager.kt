package su.plo.voice.api.server.audio.source

import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.player.VoiceServerPlayer

interface ServerSourceManager : BaseServerSourceManager {

    fun createPlayerSource(
        addonObject: Any,
        player: VoiceServerPlayer,
        line: ServerSourceLine,
        codec: String?,
        stereo: Boolean
    ): ServerPlayerSource

    fun createEntitySource(
        addonObject: Any,
        entity: MinecraftServerEntity,
        line: ServerSourceLine,
        codec: String?,
        stereo: Boolean
    ): ServerEntitySource

    fun createStaticSource(
        addonObject: Any,
        position: ServerPos3d,
        line: ServerSourceLine,
        codec: String?,
        stereo: Boolean
    ): ServerStaticSource
}
