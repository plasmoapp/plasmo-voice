package su.plo.voice.client.mac

import com.sun.jna.Platform

object AVCaptureDevice {

    val authorizationStatus: AVAuthorizationStatus
        get() {
            // https://developer.apple.com/documentation/avfoundation/avauthorizationstatus
            println(checkMacOsVersion(10, 14))
            println(System.getProperty("os.version"))
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

    private fun checkMacOsVersion(minMajor: Int, minMinor: Int): Boolean {
        if (!Platform.isMac()) return false

        val version = System.getProperty("os.version")?.split(".") ?: return true
        val major = version.getOrNull(0)?.toInt() ?: 0
        val minor = version.getOrNull(1)?.toInt() ?: 0

        return major > minMajor || (major == minMajor && minor >= minMinor)
    }
}
