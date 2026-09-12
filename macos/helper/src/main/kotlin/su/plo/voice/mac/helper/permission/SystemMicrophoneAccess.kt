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
internal object SystemMicrophoneAccess : MicrophoneAccess {
    override val status: AuthStatus
        /*
         * authorizationStatusForMediaType()
         * https://developer.apple.com/documentation/avfoundation/avcapturedevice/authorizationstatus(for:)
         */
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
            /*
             * activateIgnoringOtherApps()
             * https://developer.apple.com/documentation/appkit/nsapplication/activate(ignoringotherapps:)
             */
            NSApplication.sharedApplication.activateIgnoringOtherApps(true) // TODO: deprecated, change in future

            /*
             * requestAccessForMediaType()
             * https://developer.apple.com/documentation/avfoundation/avcapturedevice/requestaccess(for:completionhandler:)
             */
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeAudio) { _ ->
                onResult(status)
            }
        }
    }

    override fun settings() = onMainThread {
        /*
         * URLWithString()
         * https://developer.apple.com/documentation/foundation/nsurl/urlwithstring:?language=objc
         */
        NSURL.URLWithString(MICROPHONE_SETTINGS)?.let(
            /*
             * openURL()
             * https://developer.apple.com/documentation/appkit/nsworkspace/open(_:)
             */
            NSWorkspace.sharedWorkspace::openURL
        )
    }
}

/*
 * dispatch_async()
 * https://developer.apple.com/documentation/dispatch/dispatch_async
 *
 * dispatch_get_main_queue()
 * https://developer.apple.com/documentation/dispatch/dispatch_get_main_queue
 */
private fun onMainThread(block: () -> Unit) = dispatch_async(dispatch_get_main_queue(), block)
