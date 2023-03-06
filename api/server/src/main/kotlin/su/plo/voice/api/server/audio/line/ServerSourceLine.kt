package su.plo.voice.api.server.audio.line

import su.plo.lib.api.server.entity.MinecraftServerEntity
import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.server.audio.source.ServerEntitySource
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo

interface ServerSourceLine : BaseServerSourceLine {

    /**
     * Creates a new player source
     *
     * Player source is proximity source attached to specified player
     *
     * @return a new [ServerPlayerSource]
     */
    fun createPlayerSource(
        player: VoiceServerPlayer,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerPlayerSource

    /**
     * Creates a new entity source
     *
     * Player source is proximity source attached to specified entity
     *
     * @throws IllegalArgumentException when trying to create entity source for a player. Use [createPlayerSource] instead
     *
     * @return a new [ServerEntitySource]
     */
    fun createEntitySource(
        entity: MinecraftServerEntity,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerEntitySource

    /**
     * Creates a new static source
     *
     * Player source is proximity source attached to specified world position
     *
     * @return a new [ServerStaticSource]
     */
    fun createStaticSource(
        position: ServerPos3d,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerStaticSource
}
