package su.plo.voice.mac.helper.capture

/**
 * Cuts the stream into frames of one fixed size.
 *
 * Be aware that it's only ever touched from the audio callback thread.
 */
internal class FrameSlicer(private val frameBytes: Int, private val onFrame: (ByteArray) -> Unit) {
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
