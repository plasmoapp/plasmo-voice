package su.plo.voice.mac.protocol.exception

/**
 * Raised when the bytes arriving cannot be a frame at all.
 *
 * Better to fail loudly here than to hand the mod a megabyte of noise decoded as audio.
 */
class ProtocolException(val error: ProtocolError, override val message: String) : RuntimeException(message)
