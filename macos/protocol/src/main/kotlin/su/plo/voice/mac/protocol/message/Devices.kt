package su.plo.voice.mac.protocol.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import su.plo.voice.mac.protocol.audio.DeviceInfo

/**
 * Sent as a response to [Request.ListDevices] and again as an [Event] whenever macOS reports a
 * device being plugged in or pulled out.
 */
@Serializable
@SerialName("devices")
data class Devices(val devices: List<DeviceInfo>, val defaultId: String?) : Response, Event
