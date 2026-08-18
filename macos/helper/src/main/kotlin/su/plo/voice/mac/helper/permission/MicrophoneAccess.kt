package su.plo.voice.mac.helper.permission

import su.plo.voice.mac.protocol.message.AuthStatus

/**
 * The permission side of macOS, behind a door the session can knock on.
 */
internal interface MicrophoneAccess {
    /** Status of the microphone access. */
    val status: AuthStatus

    /**
     * May put a dialog in front of the player, so this can take minutes or never finish at all.
     * [onResult] arrives on a system.
     */
    fun request(onResult: (AuthStatus) -> Unit)

    /** The escape hatch for a player who said no, since macOS will not ask them twice. */
    fun settings()
}
