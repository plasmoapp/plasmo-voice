package su.plo.voice.mac.helper.permission

import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

/**
 * Reads and requests microphone access for this bundle.
 *
 * macOS decides microphone access per application bundle, not per process, and only for bundles
 * that declare `NSMicrophoneUsageDescription`. That is the entire reason this helper exists as a
 * separate app.
 */
// AVAuthorizationStatus
// su.plo.voice.client.mac
internal object Permissions {
    val status: AuthStatus
        get() = when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeAudio)) {
            AVAuthorizationStatusNotDetermined -> AuthStatus.NOT_DETERMINED
            AVAuthorizationStatusRestricted -> AuthStatus.RESTRICTED
            AVAuthorizationStatusDenied -> AuthStatus.DENIED
            AVAuthorizationStatusAuthorized -> AuthStatus.AUTHORIZED
            else -> AuthStatus.UNKNOWN
        }

    fun request(onResult: (AuthStatus) -> Unit) {
        if (status != AuthStatus.NOT_DETERMINED) return onResult(status)

        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { _ -> onResult(status) }
    }
}
