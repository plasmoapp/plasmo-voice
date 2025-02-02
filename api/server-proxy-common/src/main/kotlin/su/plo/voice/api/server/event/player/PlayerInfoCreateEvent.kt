package su.plo.voice.api.server.event.player

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.player.VoicePlayer
import su.plo.voice.proto.data.player.VoicePlayerInfo

/**
 * This event is fires when [VoicePlayer.createPlayerInfo] is invoked.
 *
 * This event can be used to change information about the player sent to the clients.
 */
data class PlayerInfoCreateEvent(
    val player: VoicePlayer,
    var voicePlayerInfo: VoicePlayerInfo
) : Event