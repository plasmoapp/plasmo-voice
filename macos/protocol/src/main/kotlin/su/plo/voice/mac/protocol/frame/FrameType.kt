package su.plo.voice.mac.protocol.frame

enum class FrameType(val code: Byte, val hasRequestId: Boolean = false) {
    REQUEST(0, hasRequestId = true),
    RESPONSE(1, hasRequestId = true),
    EVENT(2),
    AUDIO(3),
    PING(4);

    companion object {
        fun byCode(code: Byte) = entries.firstOrNull { it.code == code }
    }
}
