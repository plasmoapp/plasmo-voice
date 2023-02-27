package su.plo.voice.server.audio.source

import su.plo.lib.api.server.world.ServerPos3d
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.server.PlasmoVoiceServer
import su.plo.voice.api.server.audio.line.ServerSourceLine
import su.plo.voice.api.server.audio.source.ServerPlayerSource
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.api.server.player.VoiceServerPlayer
import su.plo.voice.proto.data.audio.source.PlayerSourceInfo
import java.util.*

class VoiceServerPlayerSource(
    voiceServer: PlasmoVoiceServer,
    addon: AddonContainer,
    line: ServerSourceLine,
    codec: String?,
    stereo: Boolean,
    override val player: VoiceServerPlayer
) : VoiceServerPositionalSource<PlayerSourceInfo>(voiceServer, addon, UUID.randomUUID(), line, codec, stereo),
    ServerPlayerSource {

    private val playerPosition = ServerPos3d()

    override val position: ServerPos3d
        get() = player.instance.getServerPosition(playerPosition)

    override val sourceInfo: PlayerSourceInfo
        get() = PlayerSourceInfo(
            addon.id,
            id,
            line.id, state.get().toByte(),
            codec,
            stereo,
            iconVisible,
            angle,
            player.createPlayerInfo()
        )

    init {
        addFilter(this::filterSelf)
        addFilter(this::filterVanish)
    }

    private fun filterSelf(player: VoicePlayer): Boolean =
        player != this.player

    private fun filterVanish(player: VoicePlayer): Boolean =
        (player as VoiceServerPlayer).instance.canSee(this.player.instance)
}
