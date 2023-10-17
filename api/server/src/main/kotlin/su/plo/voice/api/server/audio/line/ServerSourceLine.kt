package su.plo.voice.api.server.audio.line

import su.plo.slib.api.server.entity.McServerEntity
import su.plo.slib.api.server.position.ServerPos3d
import su.plo.voice.api.server.audio.source.ServerEntitySource
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.audio.source.ServerStaticSource
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo

/**
 * Represents a server source line.
 *
 * Server source lines used to create a server audio sources.
 */
interface ServerSourceLine : BaseServerSourceLine {

    /**
     * Creates a new player source.
     *
     * Player source is a proximity source attached to the specified player.
     *
     * @param player The target player.
     * @param stereo Whether the source should be stereo (default is false).
     * @param decoderInfo Optional decoder information, default is [OpusDecoderInfo].
     * @return A new [ServerPlayerSource] instance.
     */
    fun createPlayerSource(
        player: VoiceServerPlayer,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerPlayerSource

    /**
     * Creates a new entity source.
     *
     * Entity source is a proximity source attached to the specified entity.
     *
     * @param entity The target entity.
     * @param stereo Whether the source should be stereo (default is false).
     * @param decoderInfo Optional decoder information, default is [OpusDecoderInfo].
     * @throws IllegalArgumentException when trying to create an entity source for a player. Use [createPlayerSource] instead.
     * @return A new [ServerEntitySource] instance.
     */
    fun createEntitySource(
        entity: McServerEntity,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerEntitySource

    /**
     * Creates a new static source.
     *
     * Static source is a proximity source attached to the specified world position.
     *
     * @param position The world position.
     * @param stereo Whether the source should be stereo (default is false).
     * @param decoderInfo Optional decoder information, default is [OpusDecoderInfo].
     * @return A new [ServerStaticSource] instance.
     */
    fun createStaticSource(
        position: ServerPos3d,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerStaticSource
}
