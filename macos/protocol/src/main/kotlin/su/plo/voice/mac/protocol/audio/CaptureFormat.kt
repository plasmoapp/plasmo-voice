package su.plo.voice.mac.protocol.audio

import kotlinx.serialization.Serializable

/**
 * What the mod wants to receive, which has nothing to do with what the hardware runs at.
 */
@Serializable
data class CaptureFormat(val sampleRate: Int, val channels: Int)
