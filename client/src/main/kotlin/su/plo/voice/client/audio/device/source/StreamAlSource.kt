package su.plo.voice.client.audio.device.source

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.lwjgl.openal.AL11
import su.plo.voice.BaseVoice
import su.plo.voice.api.client.PlasmoVoiceClient
import su.plo.voice.api.client.audio.device.DeviceException
import su.plo.voice.api.client.audio.device.source.AlSource
import su.plo.voice.api.client.event.audio.device.source.AlSourceBufferQueuedEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceBufferUnqueuedEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceClosedEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceCreatedEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourcePauseEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourcePlayEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceStopEvent
import su.plo.voice.api.client.event.audio.device.source.AlSourceWriteEvent
import su.plo.voice.api.client.event.audio.device.source.AlStreamSourceStoppedEvent
import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.api.util.AudioUtil
import su.plo.voice.client.audio.AlUtil
import su.plo.voice.client.audio.device.AlOutputDevice
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

class StreamAlSource private constructor(
    client: PlasmoVoiceClient,
    device: AlOutputDevice,
    stereo: Boolean,
    numBuffers: Int,
    pointer: Int,
) : BaseAlSource(client, device, stereo, pointer) {

    private val timeSupplier: TimeSupplier
        get() = client.timeSupplier

    private var closeTimeoutMs = 25000L

    // +1 for silence buffer
    private val numBuffers: Int =
        (if (numBuffers == 0) client.config.advanced.alPlaybackBuffers.value() else numBuffers) + 1
    private val queue = LinkedBlockingQueue<ShortArray>()
    private val wakeUp = Channel<Unit>(Channel.CONFLATED)
    private val isStreaming = AtomicBoolean(false)

    private var job: Job? = null
    private lateinit var buffers: IntArray
    private var lastBufferTime: Long = 0

    private val freeBuffers = ArrayDeque<Int>()
    private val unqueuedBuffer = IntArray(1)

    private var currentlyPlaying = false

    override fun play() {
        AlUtil.checkDeviceContext(device)

        if (!client.eventBus.fire(AlSourcePlayEvent(this))) return

        val isStreaming = isStreaming.get()
        val state = state

        if (isStreaming && state == AlSource.State.PAUSED) {
            AL11.alSourcePlay(pointer)
            AlUtil.checkErrors("Source play")
            return
        } else if (isStreaming) {
            return
        }

        startStreamThread()
    }

    override fun stop() {
        AlUtil.checkDeviceContext(device)

        AlSourceStopEvent(this).also {
            if (!client.eventBus.fire(it)) return
        }

        AL11.alSourceStop(pointer)
        AlUtil.checkErrors("Source stop")
        isStreaming.set(false)
        currentlyPlaying = false

        clearBuffer()
    }

    override fun pause() {
        AlUtil.checkDeviceContext(device)

        AlSourcePauseEvent(this).also {
            if (!client.eventBus.fire(it)) return
        }

        AL11.alSourcePause(pointer)
        AlUtil.checkErrors("Source pause")
    }

    override fun setCloseTimeoutMs(timeoutMs: Long) {
        this.closeTimeoutMs = timeoutMs
    }

    override fun updateLastBufferTime() {
        lastBufferTime = timeSupplier.currentTimeMillis
    }

    override fun write(samples: ShortArray, applyFilters: Boolean) {
        val processedSamples =
            if (applyFilters) {
                device.processFilters(samples)
            } else {
                samples
            }

        if (!isStreaming.get()) return
        if (processedSamples.isEmpty()) return

        if (queue.size > 100) {
            BaseVoice.DEBUG_LOGGER.log("Queue overflow, dropping samples")
            return
        }

        val writeEvent = AlSourceWriteEvent(this, processedSamples)
        if (!client.eventBus.fire(writeEvent)) return

        val newSamples = writeEvent.samplesShorts

        queue.offer(newSamples)
        lastBufferTime = timeSupplier.currentTimeMillis

        wakeUp.trySend(Unit)
    }

    override fun write(samples: ByteArray) {
        write(AudioUtil.bytesToShorts(samples), false)
    }

    override fun clearBuffer() {
        if (queue.isEmpty()) return
        queue.clear()
    }

    override suspend fun close() {
        if (!isStreaming.get()) return
        device.runInContext {
            closeSync()
        }
    }

    override fun closeAsync(): CompletableFuture<Void?> {
        if (!isStreaming.get()) return CompletableFuture.completedFuture(null)

        return device.coroutineScope.future {
            closeSync()
            null
        }
    }

    private fun closeSync() {
        stop()

        // to my future self:
        // don't join job coroutine here,
        // because it can be invoked from the job itself
        // and cause a deadlock
        // (I've done it twice already pepega)
        // job?.join()

        // play a source if its state is initial, so a source can be deleted properly
        if (state == AlSource.State.INITIAL) {
            AL11.alSourcePlay(pointer)
            AlUtil.checkErrors("Source play")

            AL11.alSourceStop(pointer)
            AlUtil.checkErrors("Source stop")
        }

        client.eventBus.fire(AlSourceClosedEvent(this@StreamAlSource))

        recycleProcessedBuffers(false)

        AL11.alDeleteBuffers(buffers)
        AlUtil.checkErrors("Delete buffers")

        freeBuffers.clear()

        AL11.alDeleteSources(intArrayOf(pointer))
        AlUtil.checkErrors("Delete source")

        pointer = 0

        clearBuffer()
    }

    private fun startStreamThread() {
        isStreaming.set(true)
        val alSource = this

        job?.cancel()
        job = device.coroutineScope.launch {
            // buffers outlive stop()
            if (!::buffers.isInitialized) {
                buffers = IntArray(numBuffers)
                AL11.alGenBuffers(buffers)
                AlUtil.checkErrors("Source gen buffers")

                for (buffer in buffers) {
                    freeBuffers.addLast(buffer)
                }
            }

            updateLastBufferTime()

            while (isStreaming.get()) {
                val state = state

                recycleProcessedBuffers()

                if (state == AlSource.State.PLAYING || state == AlSource.State.PAUSED) {
                    fillQueue()
                } else {
                    if (queue.isNotEmpty()) {
                        startPlayback()
                    } else if (currentlyPlaying) {
                        currentlyPlaying = false
                        client.eventBus.fire(AlStreamSourceStoppedEvent(alSource))
                    }
                }

                if (closeTimeoutMs > 0L && timeSupplier.currentTimeMillis - lastBufferTime > closeTimeoutMs) {
                    BaseVoice.DEBUG_LOGGER.log("Stream timed out. Closing...")
                    close()
                    break
                }

                withTimeoutOrNull(5L) { wakeUp.receive() }
            }
        }
    }

    private fun startPlayback() {
        var pendingSamples = 0
        var newestSamples = 0
        for (samples in queue) {
            newestSamples = samples.size
            pendingSamples += samples.size
        }

        val sampleRate = device.format.sampleRate.toInt()
        val minQueuedSamples = (device.mixerUpdateSamples(sampleRate) + sampleRate * JITTER_MARGIN_MS / 1000) * channels
        val backlogSamples = (pendingSamples - newestSamples)
        val silenceSamples = max(MIN_SILENCE_FRAMES * channels, minQueuedSamples - backlogSamples)

        val freeBuffer = freeBuffers.removeFirstOrNull() ?: return

        if (!fillAndPushBuffer(ShortArray(silenceSamples - silenceSamples % channels), freeBuffer)) return
        fillQueue()

        AL11.alSourcePlay(pointer)
        AlUtil.checkErrors("Source play")
        currentlyPlaying = true
    }

    private fun fillQueue() {
        while (freeBuffers.isNotEmpty() && queue.isNotEmpty()) {
            val samples = queue.poll()
            val freeBuffer = freeBuffers.removeFirst()

            if (!fillAndPushBuffer(samples, freeBuffer)) return
        }
    }

    private fun fillAndPushBuffer(samples: ShortArray, buffer: Int): Boolean {
        AL11.alBufferData(buffer, format, samples, device.format.sampleRate.toInt())
        if (AlUtil.checkErrors("Assigning buffer data")) {
            freeBuffers.addLast(buffer)
            return false
        }

        AL11.alSourceQueueBuffers(pointer, intArrayOf(buffer))
        if (AlUtil.checkErrors("Queue buffer data")) {
            freeBuffers.addLast(buffer)
            return false
        }

        client.eventBus.fire(AlSourceBufferQueuedEvent(this, samples, buffer))

        return true
    }

    private fun recycleProcessedBuffers(fireEvent: Boolean = true) {
        var processedBuffers = getInt(AL11.AL_BUFFERS_PROCESSED)

        while (processedBuffers > 0) {
            AL11.alSourceUnqueueBuffers(pointer, unqueuedBuffer)
            if (AlUtil.checkErrors("Unqueue buffer")) return

            freeBuffers.addLast(unqueuedBuffer[0])
            if (fireEvent) {
                client.eventBus.fire(AlSourceBufferUnqueuedEvent(this, unqueuedBuffer[0]))
            }

            processedBuffers--
        }
    }

    companion object {
        private const val JITTER_MARGIN_MS = 10
        private const val MIN_SILENCE_FRAMES = 64

        @JvmStatic
        fun create(device: AlOutputDevice, client: PlasmoVoiceClient, stereo: Boolean, numBuffers: Int): AlSource {
            AlUtil.checkDeviceContext(device)

            val pointer = IntArray(1)
            AL11.alGenSources(pointer)
            if (AlUtil.checkErrors("Allocate new source")) {
                throw DeviceException("Failed to allocate new source")
            }

            return StreamAlSource(client, device, stereo, numBuffers, pointer[0]).also { source ->
                client.eventBus.fire(AlSourceCreatedEvent(source))
            }
        }
    }
}
