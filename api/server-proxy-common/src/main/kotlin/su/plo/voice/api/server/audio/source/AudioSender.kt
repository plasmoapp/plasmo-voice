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

    /**
     * Gets a coroutine job started by [start].
     *
     * Use this field only with [kotlin relocation](https://plasmovoice.com/docs/api/#kotlin).
     * Otherwise, you will get class not found exception.
     */
    var job: Job? = null
        private set

    private var onStop: Runnable? = null

    private var paused = false

    /**
     * Starts the coroutine job that sends the frames from the [frameProvider] each 20ms.
     *
     * This will also resume the sender if it was paused using [pause].
     */
    fun start() {
        resume()

        val job = CoroutineScope(Dispatchers.Default).launch {
            var sequenceNumber = 0L
            var startTime = 0L

            var endOfStream = false

            try {
                while (isActive) {
                    if (paused) {
                        if (!endOfStream) {
                            endOfStream = true
                            onEnd.invoke(sequenceNumber++)
                            startTime = 0L
                        }

                        delay(10L)
                        continue
                    }

                    val frame = when (val frameResult = frameProvider.provide20ms()) {
                        is AudioFrameResult.EndOfStream -> {
                            if (endOfStream) continue

                            endOfStream = true
                            onEnd.invoke(sequenceNumber++)
                            startTime = 0L
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

                    if (startTime == 0L) {
                        startTime = System.nanoTime()
                    }

                    val frameTime = 20_000_000 * sequenceNumber
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

        this.job = job
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
}
