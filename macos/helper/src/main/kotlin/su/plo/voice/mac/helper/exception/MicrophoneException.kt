package su.plo.voice.mac.helper.exception

/**
 * Microphone stopped working as it should work.
 */
internal class MicrophoneException(val error: MicrophoneError, override val message: String) : RuntimeException(message)
