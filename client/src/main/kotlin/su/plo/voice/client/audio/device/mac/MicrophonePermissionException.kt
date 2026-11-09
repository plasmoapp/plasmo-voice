package su.plo.voice.client.audio.device.mac

import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.mac.protocol.message.status.AuthStatus

/**
 * macOS hasn't given the microphone to whoever asked for it, the helper or the launcher.
 */
internal class MicrophonePermissionException(val status: AuthStatus) :
    DeviceException("Microphone access is $status")
