package su.plo.voice.client.audio.device.source

import org.lwjgl.system.MemoryUtil
import java.nio.Buffer
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class ByteBufferPool(
    maxPooledBuffer: Int,
) {
    private val bufferPool = ArrayBlockingQueue<ByteBuffer>(maxPooledBuffer)

    fun acquire(capacity: Int): ByteBuffer {
        while (true) {
            val pooled = bufferPool.poll() ?: break
            if (pooled.capacity() >= capacity) {
                (pooled as Buffer).clear()
                return pooled
            }

            MemoryUtil.memFree(pooled)
        }

        return MemoryUtil.memAlloc(capacity)
    }

    fun release(buffer: ByteBuffer) {
        if (!bufferPool.offer(buffer)) {
            MemoryUtil.memFree(buffer)
        }
    }

    fun free() {
        while (true) {
            val buffer = bufferPool.poll() ?: break
            MemoryUtil.memFree(buffer)
        }
    }
}
