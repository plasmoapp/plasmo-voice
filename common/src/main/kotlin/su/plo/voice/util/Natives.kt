package su.plo.voice.util

fun isNativesSupported(): Boolean {
    if (System.getProperty("plasmovoice.disable_natives", "false").toBooleanStrictOrNull() == true)
        return false

    if (!isMac()) return true

    return checkMacOsVersion(11, 0)
}
