package su.plo.voice.api.server.event.mute

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.api.server.mute.ServerMuteInfo

/**
 * This event is fired once player was muted in [MuteManager]
 */
class PlayerVoiceMutedEvent(
    val muteManager: MuteManager,
    val muteInfo: ServerMuteInfo
) : Event
