package su.plo.voice.util

import com.sun.jna.Platform

fun isNativesSupported(): Boolean {
    if (System.getProperty("plasmovoice.disable_natives", "false").toBooleanStrictOrNull() == true)
        return false

    if (!Platform.isMac()) return true

    return checkMacOsVersion(11, 0)
}
