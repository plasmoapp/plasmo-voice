package su.plo.voice.mac.protocol.exception

import su.plo.voice.mac.protocol.message.status.FailureCode

enum class ProtocolError(val code: FailureCode) {
    UNKNOWN_FRAME_TYPE(FailureCode.INTERNAL),
    FRAME_SIZE_OUT_OF_BOUNDS(FailureCode.INTERNAL);

    fun exception(detail: Any? = null): ProtocolException = ProtocolException(
        this,
        when (this) {
            UNKNOWN_FRAME_TYPE -> "Unknown frame type $detail."
            FRAME_SIZE_OUT_OF_BOUNDS -> "Frame size $detail is out of bounds."
        }
    )
}
