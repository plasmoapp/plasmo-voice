package su.plo.voice.mac.helper.permission

import su.plo.voice.mac.protocol.message.status.AuthStatus

internal interface MicrophoneAccess {
    val status: AuthStatus

    /**
     * Returns immediately, having at most put a dialog in front of the player, which they may sit
     * on for minutes or never answer at all.
     *
     * [onResult] arrives later, on a system thread.
     */
    fun request(onResult: (AuthStatus) -> Unit)

    /** The escape hatch for a player who said no, since macOS will not ask them twice. */
    fun settings()
}
