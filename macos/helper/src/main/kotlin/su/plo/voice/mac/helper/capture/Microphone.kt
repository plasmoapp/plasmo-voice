package su.plo.voice.mac.helper.capture

import su.plo.voice.mac.protocol.audio.CaptureFormat

internal const val FRAME_MILLIS = 20

/**
 * A source of ready to send audio frames.
 */
internal interface Microphone {
    /**
     * Starts capturing in [format] and calls [onFrame] once per frame, on whichever thread the
     * audio system uses.
     *
     * A null [deviceId] means the current system default input.
     *
     * @return how many samples each frame carries.
     */
    fun open(format: CaptureFormat, deviceId: String?, onFrame: (ByteArray) -> Unit): Int

    /**
     * Closes capturing.
     */
    fun close()
}

internal fun CaptureFormat.frameSamples() = sampleRate / 1000 * FRAME_MILLIS * channels
