package su.plo.voice.mac.protocol.exception

/**
 * Raised when the bytes arriving cannot be a frame at all.
 */
class ProtocolException(val error: ProtocolError, override val message: String) : RuntimeException(message)
