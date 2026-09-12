package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.plo.voice.mac.protocol.audio.CaptureFormat

/**
 * Everything Minecraft is allowed to ask for.
 *
 * The mod holds the wheel. The helper never decides on its own to show a prompt or to switch the
 * microphone on, because both are things the player would notice happening behind their back.
 */
@Serializable
sealed interface Upstream {
    /**
     * Asked before drawing the settings screen, and again when the player presses the button there.
     *
     * [prompt] separates the two: without it the answer is instant and silent, with it macOS may
     * put a dialog in front of the player, which can take as long as they need.
     */
    @Serializable
    @SerialName("permission")
    data class Permission(val prompt: Boolean, val requestId: Int = 0) : Upstream

    /**
     * Sent when the player has already denied access once.
     *
     * macOS never shows the prompt a second time, so system settings are the only way back and the
     * mod has to send the player there by hand.
     */
    @Serializable
    @SerialName("settings")
    data object OpenSettings : Upstream

    /**
     * Asks for the current input list.
     *
     * The helper also sends it unprompted when it changes.
     */
    @Serializable
    @SerialName("devices")
    data class ListDevices(val requestId: Int = 0) : Upstream

    /**
     * Starts the microphone.
     *
     * Audio keeps coming until [Close] or until the connection drops.
     */
    @Serializable
    @SerialName("open")
    data class Open(val format: CaptureFormat, val deviceId: String? = null, val requestId: Int = 0) : Upstream

    /**
     * Releases the microphone without shutting the helper down.
     *
     * Worth doing whenever the player is not talking: while a microphone is open, macOS drops
     * bluetooth headphones into call mode and the music quality goes with it.
     */
    @Serializable
    @SerialName("close")
    data class Close(val requestId: Int = 0) : Upstream
}
