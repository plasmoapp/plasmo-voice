package su.plo.voice.util

import com.sun.jna.Platform

fun checkMacOsVersion(minMajor: Int, minMinor: Int): Boolean {
    if (!Platform.isMac()) return false

    val version = System.getProperty("os.version")?.split(".") ?: return true
    val major = version.getOrNull(0)?.toInt() ?: 0
    val minor = version.getOrNull(1)?.toInt() ?: 0

    return major > minMajor || (major == minMajor && minor >= minMinor)
}
