package su.plo.voice.api.server.event.mute

import su.plo.voice.api.event.Event
import su.plo.voice.api.server.mute.MuteManager
import su.plo.voice.api.server.mute.ServerMuteInfo

/**
 * This event is fired when a player is muted in the [MuteManager].
 *
 * The event is fired on the server thread (the global region thread on Folia),
 * not necessarily before [MuteManager.mute] returns.
 * Use [muteInfo] instead of [MuteManager.getMute], the mute may already be removed by then.
 */
class PlayerVoiceMutedEvent(
    val muteManager: MuteManager,
    val muteInfo: ServerMuteInfo
) : Event
