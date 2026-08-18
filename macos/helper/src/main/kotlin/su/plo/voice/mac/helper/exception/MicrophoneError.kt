package su.plo.voice.mac.helper.exception

/**
 * Every microphone error.
 */
internal enum class MicrophoneError {
    QUEUE_CREATION_FAILED,
    CORE_AUDIO_STATUS;

    fun exception(detail: Any? = null): MicrophoneException = MicrophoneException(
        when (this) {
            QUEUE_CREATION_FAILED -> "AudioQueue creation failed."
            CORE_AUDIO_STATUS -> "CoreAudio status: $detail."
        }
    )
}
