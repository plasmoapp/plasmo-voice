package su.plo.voice.mac.helper.exception

import su.plo.voice.mac.protocol.message.status.FailureCode

internal enum class MicrophoneError(val code: FailureCode) {
    QUEUE_CREATION_FAILED(FailureCode.INTERNAL),
    CORE_AUDIO_STATUS(FailureCode.INTERNAL),
    DEVICE_NOT_FOUND(FailureCode.DEVICE_NOT_FOUND);

    fun exception(detail: Any? = null): MicrophoneException = MicrophoneException(
        this,
        when (this) {
            QUEUE_CREATION_FAILED -> "AudioQueue creation failed."
            CORE_AUDIO_STATUS -> "CoreAudio status: $detail."
            DEVICE_NOT_FOUND -> "No such input device: $detail."
        }
    )
}
