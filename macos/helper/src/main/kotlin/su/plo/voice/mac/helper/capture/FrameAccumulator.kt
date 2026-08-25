package su.plo.voice.mac.helper.capture

/**
 * Buffers arbitrarily-sized chunks of PCM bytes from audio queue and emits them as
 * fixed-size frames once enough data has piled up.
 *
 * Be aware that it's only ever touched from the audio callback thread.
 */
internal class FrameAccumulator(private val frameBytes: Int, private val onFrame: (ByteArray) -> Unit) {
    private val pending = ByteArray(frameBytes)
    private var filled = 0

    fun write(bytes: ByteArray) {
        var taken = 0
        while (taken < bytes.size) {
            val chunk = minOf(frameBytes - filled, bytes.size - taken)
            bytes.copyInto(pending, filled, taken, taken + chunk)

            filled += chunk
            taken += chunk

            if (filled == frameBytes) {
                onFrame(pending.copyOf())
                filled = 0
            }
        }
    }
}
