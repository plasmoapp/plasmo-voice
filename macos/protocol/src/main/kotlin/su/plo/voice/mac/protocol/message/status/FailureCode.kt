package su.plo.voice.mac.protocol.message.status

/**
 * Why capture could not happen, coarse enough that the mod can pick a screen without parsing text.
 */
enum class FailureCode {
    PERMISSION_DENIED,
    DEVICE_NOT_FOUND,
    INTERNAL

    // TODO: add DEVICE_BUSY, FORMAT_UNSUPPORTED in future
}
