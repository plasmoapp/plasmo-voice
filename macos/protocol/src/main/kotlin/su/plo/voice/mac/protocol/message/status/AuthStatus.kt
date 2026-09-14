package su.plo.voice.mac.protocol.message.status

/**
 * The macOS answer to "may this app use the microphone?".
 *
 * Only [NOT_DETERMINED] leaves the door open, since that is the single state in which macOS is
 * still willing to show a prompt. Everything else is already decided and asking again does nothing,
 * which is why the mod has to offer system settings instead of a retry button.
 */
enum class AuthStatus {
    NOT_DETERMINED,
    RESTRICTED,
    DENIED,
    AUTHORIZED,
    UNKNOWN
}
