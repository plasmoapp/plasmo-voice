package su.plo.voice.mac.helper.permission

import platform.AVFoundation.*
import platform.AppKit.NSApplication
import platform.AppKit.NSWorkspace
import platform.Foundation.NSURL
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
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

        onMainThread {
            NSApplication.sharedApplication.activateIgnoringOtherApps(true)

            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { _ ->
                onResult(status)
            }
        }
    }

    override fun settings() = onMainThread {
        NSURL.URLWithString(MICROPHONE_SETTINGS)?.let(NSWorkspace.sharedWorkspace::openURL)
    }
}

private fun onMainThread(block: () -> Unit) = dispatch_async(dispatch_get_main_queue(), block)
