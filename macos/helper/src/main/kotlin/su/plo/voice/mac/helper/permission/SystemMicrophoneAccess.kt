package su.plo.voice.mac.helper.permission

import platform.AVFoundation.*
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL
import su.plo.voice.mac.protocol.message.status.AuthStatus

private const val MICROPHONE_SETTINGS = "x-apple.systempreferences:com.apple.preference.security?Privacy_Microphone"

/**
 * System microphone accessor.
 *
 * macOS grants the microphone to a signed bundle that declares `NSMicrophoneUsageDescription`.
 * Minecraft doesn't have this permission, so here we are.
 */
// AVAuthorizationStatus
// su.plo.voice.client.mac
internal object SystemMicrophoneAccess : MicrophoneAccess {
    override val status: AuthStatus
        get() = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)) {
            AVAuthorizationStatusNotDetermined -> AuthStatus.NOT_DETERMINED
            AVAuthorizationStatusRestricted -> AuthStatus.RESTRICTED
            AVAuthorizationStatusDenied -> AuthStatus.DENIED
            AVAuthorizationStatusAuthorized -> AuthStatus.AUTHORIZED
            else -> AuthStatus.UNKNOWN
        }

    override fun request(onResult: (AuthStatus) -> Unit) {
        if (status != AuthStatus.NOT_DETERMINED) {
            onResult(status)
            return
        }

        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { _ ->
            onResult(status)
        }
    }

    override fun settings() {
        NSURL.URLWithString(MICROPHONE_SETTINGS)?.let(NSWorkspace.sharedWorkspace::openURL)
    }
}
