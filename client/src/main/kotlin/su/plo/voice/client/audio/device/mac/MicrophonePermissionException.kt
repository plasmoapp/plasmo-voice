package su.plo.voice.client.audio.device.mac

import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.mac.protocol.message.status.AuthStatus

/**
 * The helper is up and answering, and macOS is still saying no.
 *
 * It would be better to telling this apart from every other reason a device
 * fails to open.
 */
internal class MicrophonePermissionException(val status: AuthStatus) :
    DeviceException("Microphone access is $status")
