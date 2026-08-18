package su.plo.voice.mac.helper.connection

import platform.darwin.dispatch_async
import platform.darwin.dispatch_queue_create
import su.plo.voice.mac.helper.exception.ConnectionLostException
import su.plo.voice.mac.protocol.frame.Frame
import su.plo.voice.mac.protocol.frame.FrameWriter

/**
 * Somewhere to put a frame, without the caller having to care what happens next.
 *
 * The session hands off frames from three different threads and should not have to think about any
 * of that, or about the connection being gone by the time they leave.
 */
internal interface FrameSink {
    fun send(frame: Frame)
}

/**
 * Sends everything through a single queue.
 *
 * Permission answers arrive on a system thread and audio on the render thread.
 */
internal class SerialWriter(private val writer: FrameWriter) : FrameSink {
    private val queue = dispatch_queue_create("com.plasmoverse.plasmovoice.mic.writer", null)

    override fun send(frame: Frame) {
        dispatch_async(queue) {
            try {
                writer.write(frame)
            } catch (_: ConnectionLostException) {
            }
        }
    }
}
