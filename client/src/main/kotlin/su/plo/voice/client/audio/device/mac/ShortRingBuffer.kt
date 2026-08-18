package su.plo.voice.client.audio.device.mac

/**
 * Fixed size sample buffer between the helper's reader thread and the capture thread.
 *
 * On overflow the oldest samples go first: a late frame is worth less than a growing delay.
 */
internal class ShortRingBuffer(private val capacity: Int) {
    private val buffer = ShortArray(capacity)
    private var head = 0
    private var size = 0

    val available: Int

    @Synchronized
    get() = size

    @Synchronized
    fun write(samples: ShortArray) {
        val offset = maxOf(0, samples.size - capacity)
        val length = samples.size - offset
        val tail = (head + size) % capacity
        val chunk = minOf(length, capacity - tail)

        samples.copyInto(buffer, tail, offset, offset + chunk)
        samples.copyInto(buffer, 0, offset + chunk, offset + length)

        size += length
        if (size > capacity) {
            head = (head + size - capacity) % capacity
            size = capacity
        }
    }

    @Synchronized
    fun read(length: Int): ShortArray? {
        if (length > size) return null

        val samples = ShortArray(length)
        val chunk = minOf(length, capacity - head)
        buffer.copyInto(samples, 0, head, head + chunk)
        buffer.copyInto(samples, chunk, 0, length - chunk)

        head = (head + length) % capacity
        size -= length

        return samples
    }

    @Synchronized
    fun clear() {
        head = 0
        size = 0
    }
}
