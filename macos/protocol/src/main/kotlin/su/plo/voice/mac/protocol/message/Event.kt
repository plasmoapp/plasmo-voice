package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Everything the helper says without being asked.
 */
@Serializable
sealed interface Event {
    /**
     * The first thing the helper ever sends.
     *
     * Anyone on the machine can reach a loopback port, so until the token comes back matching, the
     * mod has no reason to believe the process on the other end is the one it just launched.
     */
    @Serializable
    @SerialName("hello")
    data class Hello(val token: String, val pid: Int, val version: Int) : Event
}
