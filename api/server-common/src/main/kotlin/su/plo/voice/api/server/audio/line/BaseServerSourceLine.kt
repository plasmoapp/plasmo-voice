package su.plo.voice.api.server.audio.line

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.audio.source.AudioSourceManager
import su.plo.voice.api.server.audio.source.ServerAudioSource
import su.plo.voice.api.server.audio.source.ServerBroadcastSource
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo
import su.plo.voice.proto.data.audio.line.SourceLine
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import java.util.*

/**
 * Represents a base server source line.
 *
 * Server source lines can create new server audio sources.
 */
interface BaseServerSourceLine : SourceLine, AudioSourceManager<ServerAudioSource<*>> {

    /**
     * Gets the source line's addon.
     *
     * @return the source line's addon
     */
    val addon: AddonContainer

    /**
     * Gets the [ServerPlayerSetManager] if the source line was created with "withPlayers" argument;
     * otherwise, returns null.
     *
     * @return [ServerPlayerSetManager] if source line includes players; otherwise, null
     */
    val playerSetManager: ServerPlayerSetManager?

    /**
     * Gets the source line for a specified player.
     *
     * By default, it returns the current source line.
     * However, if [playerSetManager] is not null, an individual source line with players
     * from [ServerPlayerSet] obtained from [ServerPlayerSetManager] will be created for the player.
     *
     * @param player the player for whom to get the source line
     * @return a source line specific to the player
     */
    fun getSourceLineForPlayer(player: VoicePlayer): VoiceSourceLine

    /**
     * Creates a new direct source.
     *
     * Direct sources are used to send audio data directly to the players.
     *
     * @param player The player attached to the direct source.
     * @param stereo Whether the source should be stereo (default is false).
     * @param decoderInfo Optional decoder information, default is [OpusDecoderInfo].
     * @return A new [ServerDirectSource] instance.
     */
    fun createDirectSource(
        player: VoicePlayer,
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerDirectSource

    /**
     * @see createDirectSource
     */
    fun createDirectSource(
        player: VoicePlayer,
        stereo: Boolean = false
    ): ServerDirectSource = createDirectSource(player, stereo, OpusDecoderInfo())

    /**
     * Creates a new broadcast source.
     *
     * Broadcast sources are used to send audio data directly to the group of players.
     * By default, it sends packets to all players with Plasmo Voice installed,
     * but you can change a group of players using [ServerBroadcastSource.players]
     *
     * @param stereo Whether the source should be stereo (default is false).
     * @param decoderInfo Optional decoder information, default is [OpusDecoderInfo].
     * @return A new [ServerBroadcastSource] instance.
     */
    fun createBroadcastSource(
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerBroadcastSource

    /**
     * @see createBroadcastSource
     */
    fun createBroadcastSource(
        stereo: Boolean = false
    ): ServerBroadcastSource = createBroadcastSource(stereo, OpusDecoderInfo())

    /**
     * Removes a source from the source map by its unique identifier.
     *
     * @param sourceId the unique identifier of the source to remove.
     */
    fun removeSource(sourceId: UUID)

    /**
     * Removes a source from the source map.
     *
     * @param source The source to remove.
     */
    fun removeSource(source: ServerAudioSource<*>) =
        removeSource(source.id)

    /**
     * Interface for building a server source lines.
     *
     * @param T the type of the source line to build.
     */
    interface Builder<T : BaseServerSourceLine> {

        /**
         * Sets whether the line should include players.
         *
         * @param withPlayers Whether the source line should be with [ServerPlayerSetManager], default `false`.
         *
         * @see [BaseServerSourceLine.playerSetManager]
         */
        fun withPlayers(withPlayers: Boolean): Builder<T>

        /**
         * Sets the line's default volume.
         *
         * Default: 1.0; Min: 0.0; Max: 1.0
         *
         * @param defaultVolume The default volume value.
         */
        fun setDefaultVolume(defaultVolume: Double): Builder<T>

        /**
         * Builds and registers the source line.
         *
         * @return the built source line
         */
        fun build(): T
    }
}
