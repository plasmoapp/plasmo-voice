package su.plo.voice.mac.protocol.message

/**
 * Why capture could not happen, coarse enough that the mod can pick a screen without parsing text.
 */
enum class FailureCode {
    PERMISSION_DENIED,
    INTERNAL

    // TODO: add DEVICE_NOT_FOUND, DEVICE_BUSY, FORMAT_UNSUPPORTED in future
}
