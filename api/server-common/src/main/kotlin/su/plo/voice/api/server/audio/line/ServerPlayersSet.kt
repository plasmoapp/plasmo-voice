package su.plo.voice.api.server.audio.line

import su.plo.voice.api.server.player.VoicePlayer
import java.util.*

interface ServerPlayersSet {

    fun addPlayer(player: VoicePlayer)

    fun removePlayer(playerId: UUID): Boolean

    fun clearPlayers()

    fun getPlayers(): Collection<VoicePlayer>
}
