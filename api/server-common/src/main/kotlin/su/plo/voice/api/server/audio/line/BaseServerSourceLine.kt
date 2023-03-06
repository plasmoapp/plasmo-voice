package su.plo.voice.api.server.audio.line

import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.audio.source.AudioSourceManager
import su.plo.voice.api.server.audio.source.ServerAudioSource
import su.plo.voice.api.server.audio.source.ServerDirectSource
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.audio.codec.CodecInfo
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo
import su.plo.voice.proto.data.audio.line.SourceLine
import su.plo.voice.proto.data.audio.line.VoiceSourceLine
import java.util.*

interface BaseServerSourceLine : SourceLine, AudioSourceManager<ServerAudioSource<*>> {

    /**
     * @return the source line addon
     */
    val addon: AddonContainer

    /**
     * @return [ServerSourceLinePlayersSets] if source line was created with "withPlayers" argument;
     * otherwise null
     */
    val playersSets: ServerSourceLinePlayersSets?

    /**
     * Gets the source line for specified player
     *
     * By default, returns current source line. But if [getPlayersSets] is not null,
     * individual source line with players from [ServerPlayersSet] obtained from [ServerSourceLinePlayersSets]
     * will be created for the player.
     */
    fun getSourceLineForPlayer(player: VoicePlayer): VoiceSourceLine

    /**
     * Creates a new direct source
     *
     * Direct sources are used to send audio data directly to the players
     *
     * @return a new [ServerDirectSource]
     */
    fun createDirectSource(
        stereo: Boolean = false,
        decoderInfo: CodecInfo? = OpusDecoderInfo()
    ): ServerDirectSource

    /**
     * Removes the source from the sources map
     */
    fun removeSource(sourceId: UUID)

    /**
     * Removes the source from the sources map
     */
    fun removeSource(source: ServerAudioSource<*>) =
        removeSource(source.id)
}
