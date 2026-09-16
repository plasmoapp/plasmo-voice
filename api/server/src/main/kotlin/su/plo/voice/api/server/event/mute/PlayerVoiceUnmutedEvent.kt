package su.plo.voice.api.server.event.mute

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.api.server.mute.ServerMuteInfo

/**
 * This event is fired when a player is unmuted in the [MuteManager].
 *
 * The event is fired on the server thread (the global region thread on Folia),
 * not necessarily before [MuteManager.unmute] returns.
 */
class PlayerVoiceUnmutedEvent(
    val muteManager: MuteManager,
    val muteInfo: ServerMuteInfo
) : Event
