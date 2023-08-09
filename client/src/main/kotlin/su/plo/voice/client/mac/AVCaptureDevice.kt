package su.plo.voice.client.mac

import su.plo.voice.util.checkMacOsVersion

object AVCaptureDevice {

    val authorizationStatus: AVAuthorizationStatus
        get() {
            // https://developer.apple.com/documentation/avfoundation/avauthorizationstatus
            if (!checkMacOsVersion(10, 14))
                return AVAuthorizationStatus.AUTHORIZED

            return AVAuthorizationStatus.fromValue(
                AVFoundation.INSTANCE.objc_msgSend(
                    AVFoundation.INSTANCE.objc_getClass("AVCaptureDevice"),
                    AVFoundation.INSTANCE.sel_registerName("authorizationStatusForMediaType:"),
                    Foundation.getNSString("soun")
                )
            )
        }
}
