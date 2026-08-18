package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.plo.voice.mac.protocol.audio.CaptureFormat
import su.plo.voice.mac.protocol.audio.DeviceInfo

/**
 * Everything the helper can say back.
 *
 * Most of these answer something the mod asked for, so it can simply wait for one. [Failure] is the
 * exception and can turn up at any moment, because microphones get unplugged mid-sentence.
 */
@Serializable
sealed interface Downstream {
    /**
     * The first thing the helper ever sends.
     *
     * Anyone on the machine can reach a loopback port, so until the token comes back matching, the
     * mod has no reason to believe the process on the other end is the one it just launched.
     */
    @Serializable
    @SerialName("hello")
    data class Hello(val token: String, val pid: Int, val version: Int) : Downstream

    /**
     * Decides what the settings screen offers the player: a button that asks, a link into system
     * settings, or nothing at all because access is already granted.
     */
    @Serializable
    @SerialName("permission")
    data class Permission(val status: AuthStatus) : Downstream

    /**
     * The inputs the player can choose from, sent on request and again whenever macOS reports a
     * device being plugged in or pulled out.
     *
     * [defaultId] is the system default, so the mod can mark it without guessing.
     */
    @Serializable
    @SerialName("devices")
    data class Devices(val devices: List<DeviceInfo>, val defaultId: String?) : Downstream

    /**
     * Audio is on its way.
     *
     * [frameSamples] is how much every audio frame carries, which the mod needs before it can size
     * the buffer it reads from.
     */
    @Serializable
    @SerialName("opened")
    data class Opened(val format: CaptureFormat, val frameSamples: Int) : Downstream

    /** The microphone is released, and any headphones that dropped into call mode can recover. */
    @Serializable
    @SerialName("closed")
    data object Closed : Downstream

    /**
     * Said out loud on purpose.
     *
     * The bug this whole helper exists for is a microphone that fails silently and hands back
     * digital silence, so anything going wrong here is worth showing the player instead.
     */
    @Serializable
    @SerialName("failure")
    data class Failure(val code: FailureCode, val message: String) : Downstream
}
