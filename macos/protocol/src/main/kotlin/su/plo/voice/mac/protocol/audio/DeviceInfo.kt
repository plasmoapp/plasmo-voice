package su.plo.voice.mac.protocol.audio

import kotlinx.serialization.Serializable

/**
 * One input the player can pick.
 *
 * [id] is the `CoreAudio` unique ID, which can survive reboots, unlike the numeric device ID
 * macOS hands out at runtime.
 *
 * [name] is only ever shown to the player.
 */
@Serializable
data class DeviceInfo(val id: String, val name: String)
