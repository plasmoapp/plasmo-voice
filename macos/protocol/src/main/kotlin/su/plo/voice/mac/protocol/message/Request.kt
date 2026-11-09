package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.plo.voice.mac.protocol.audio.CaptureFormat

/**
 * Everything Minecraft is allowed to ask for, each answered by exactly one [Response].
 *
 * The mod holds the wheel. The helper never decides on its own to show a prompt or to switch the
 * microphone on, because both are things the player would notice happening behind their back.
 */
@Serializable
sealed interface Request {
    /**
     * Asked before drawing the settings screen, and again when the player presses the button there.
     *
     * [prompt] separates the two: without it the answer is instant and silent, with it macOS may
     * put a dialog in front of the player, which can take as long as they need.
     *
     * Answered with [Response.Permission].
     */
    @Serializable
    @SerialName("permission")
    data class Permission(val prompt: Boolean) : Request

    /**
     * Sent when the player has already denied access once.
     *
     * macOS never shows the prompt a second time, so system settings are the only way back and the
     * mod has to send the player there by hand.
     *
     * Answered with [Response.SettingsOpened].
     */
    @Serializable
    @SerialName("settings")
    data object OpenSettings : Request

    /**
     * Asks for the current input list.
     *
     * Answered with [Devices], which the helper also sends unprompted as an [Event] when the list
     * changes.
     */
    @Serializable
    @SerialName("devices")
    data object ListDevices : Request

    /**
     * Starts the microphone.
     *
     * Audio keeps coming until [Close] or until the connection drops.
     *
     * Answered with [Response.Opened] or [Response.Failure].
     */
    @Serializable
    @SerialName("open")
    data class Open(val format: CaptureFormat, val deviceId: String? = null) : Request

    /**
     * Releases the microphone without shutting the helper down.
     *
     * Worth doing whenever the player is not talking: while a microphone is open, macOS drops
     * bluetooth headphones into call mode and the music quality goes with it.
     *
     * Answered with [Response.Closed].
     */
    @Serializable
    @SerialName("close")
    data object Close : Request
}
