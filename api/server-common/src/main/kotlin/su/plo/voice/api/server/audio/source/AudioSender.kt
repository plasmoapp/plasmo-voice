package su.plo.voice.api.server.audio.source

import kotlinx.coroutines.*
import kotlinx.coroutines.Runnable
import su.plo.voice.api.server.audio.provider.AudioFrameProvider
import su.plo.voice.api.server.audio.provider.AudioFrameResult
import kotlin.time.Duration.Companion.nanoseconds

/**
 * Class for sending audio frames provided by [AudioFrameProvider] to the source.
 */
class AudioSender(
    private val frameProvider: AudioFrameProvider,

    private val onFrame: (frame: ByteArray, sequenceNumber: Long) -> Boolean,
    private val onEnd: (sequenceNumber: Long) -> Boolean
) {

    private var job: Job? = null

    private var onStop: Runnable? = null

    private var paused = false

    /**
     * Starts the coroutine job that sends the frames from the [frameProvider] each 20ms.
     *
     * This will also resume the sender if it was paused using [pause].
     */
    fun start() {
        resume()

        this.job = CoroutineScope(Dispatchers.Default).launch {
            var sequenceNumber = 0L
            val startTime = System.nanoTime()

            var endOfStream = false

            try {
                while (isActive) {
                    if (paused) {
                        if (!endOfStream) {
                            endOfStream = true
                            onEnd.invoke(sequenceNumber++)
                        }

                        delay(10L)
                        continue
                    }

                    val frame = when (val frameResult = frameProvider.provide20ms()) {
                        is AudioFrameResult.EndOfStream -> {
                            if (endOfStream) continue

                            endOfStream = true
                            onEnd.invoke(sequenceNumber++)
                            continue
                        }

                        is AudioFrameResult.Finished ->
                            break

                        is AudioFrameResult.Provided ->
                            frameResult.frame
                    }

                    if (frame == null) {
                        delay(5L)
                        continue
                    }

                    val frameTime = 20 * sequenceNumber
                    val waitTime = startTime + frameTime - System.nanoTime()

                    delay(waitTime.nanoseconds)

                    endOfStream = false
                    onFrame.invoke(frame, sequenceNumber++)
                }
            } finally {
                withContext(NonCancellable) {
                    onEnd.invoke(sequenceNumber)

                    onStop?.run()
                }
            }
        }
    }

    /**
     * Stops the sender coroutine job if its active.
     */
    fun stop() {
        job?.cancel()
    }

    /**
     * Pauses the audio sender.
     */
    fun pause() {
        this.paused = true
    }

    /**
     * Resumes the audio sender.
     */
    fun resume() {
        this.paused = false
    }

    /**
     * Set the runnable that will be invoked when the sender coroutine job stops.
     *
     * @param onStop The runnable.
     */
    fun onStop(onStop: Runnable?) {
        this.onStop = onStop
    }

    /**
     * Joins the sender coroutine job if its active.
     */
    @JvmSynthetic
    suspend fun join() {
        job?.join()
    }
}
