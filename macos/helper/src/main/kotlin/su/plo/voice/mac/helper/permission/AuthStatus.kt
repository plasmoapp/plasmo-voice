package su.plo.voice.mac.helper.permission

/**
 * The macOS answer to "may this app use the microphone?".
 *
 * [NOT_DETERMINED] means the user has never been asked, so a prompt is still possible. Every other
 * value is a decision the system already stored, and asking again changes nothing.
 */
internal enum class AuthStatus {
    NOT_DETERMINED, RESTRICTED, DENIED, AUTHORIZED, UNKNOWN
}
