package su.plo.voice.api.server.event.mute

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.api.server.mute.ServerMuteInfo

/**
 * This event is fired when a player is muted in the [MuteManager].
 */
class PlayerVoiceMutedEvent(
    val muteManager: MuteManager,
    val muteInfo: ServerMuteInfo
) : Event
