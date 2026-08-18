package su.plo.voice.mac.protocol.frame

/**
 * What kind of payload follows on the wire.
 */
enum class FrameType(val code: Byte) {
    /** One of the messages from the message package. */
    CONTROL(0),

    /** One capture frame. */
    AUDIO(1),

    /**
     * A dead helper is noticed the moment the socket closes, but a wedged one never closes
     * anything, and this is how that gets caught.
     */
    PING(2);

    companion object {
        fun byCode(code: Byte) = entries.firstOrNull { it.code == code }
    }
}
