package su.plo.voice.mac.helper.exception

/**
 * Microphone stopped working as it should work.
 */
internal class MicrophoneException(override val message: String) : RuntimeException(message)
