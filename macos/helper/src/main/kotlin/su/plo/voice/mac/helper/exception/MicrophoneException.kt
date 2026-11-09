package su.plo.voice.mac.helper.exception

internal class MicrophoneException(val error: MicrophoneError, override val message: String) : RuntimeException(message)
