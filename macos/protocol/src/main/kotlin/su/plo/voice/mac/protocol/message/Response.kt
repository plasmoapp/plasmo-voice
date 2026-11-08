package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.message.status.AuthStatus
import su.plo.voice.mac.protocol.message.status.FailureCode

/**
 * Everything the helper can answer a [Request] with.
 */
@Serializable
sealed interface Response {
    /**
     * Decides what the settings screen offers the player: a button that asks, a link into system
     * settings, or nothing at all because access is already granted.
     */
    @Serializable
    @SerialName("permission")
    data class Permission(val status: AuthStatus) : Response

    /** macOS was asked to open system settings on the microphone page. */
    @Serializable
    @SerialName("settings_opened")
    data object SettingsOpened : Response

    /**
     * Audio is on its way.
     *
     * [frameSamples] is how much every audio frame carries, which the mod needs before it can size
     * the buffer it reads from.
     */
    @Serializable
    @SerialName("opened")
    data class Opened(val format: CaptureFormat, val frameSamples: Int) : Response

    /** The microphone is released, and any headphones that dropped into call mode can recover. */
    @Serializable
    @SerialName("closed")
    data object Closed : Response

    /**
     * Said out loud on purpose.
     *
     * The bug this whole helper exists for is a microphone that fails silently and hands back
     * digital silence, so anything going wrong here is worth showing the player instead.
     */
    @Serializable
    @SerialName("failure")
    data class Failure(val code: FailureCode, val message: String) : Response
}
