package su.plo.voice.mac.helper.capture

import kotlinx.cinterop.*
import platform.AudioToolbox.*
import platform.CoreAudioTypes.*
import platform.CoreFoundation.*
import platform.darwin.OSStatus
import su.plo.voice.mac.helper.exception.MicrophoneError
import su.plo.voice.mac.protocol.audio.CaptureFormat

private const val BITS_PER_SAMPLE = 16
private const val BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8
private const val BUFFER_COUNT = 3

/**
 * Records from the system default microphone.
 */
@OptIn(ExperimentalForeignApi::class)
internal class AudioQueueMicrophone : Microphone {
    private var queue: AudioQueueRef? = null
    private var reference: StableRef<AudioQueueMicrophone>? = null
    private var slicer: FrameSlicer? = null

    override fun open(format: CaptureFormat, deviceId: String?, onFrame: (ByteArray) -> Unit): Int = runCatching {
        close()

        val frameSamples = format.frameSamples()
        val frameBytes = frameSamples * BYTES_PER_SAMPLE
        slicer = FrameSlicer(frameBytes, onFrame)

        val owner = StableRef.create(this@AudioQueueMicrophone).also { reference = it }

        memScoped {
            val description = alloc<AudioStreamBasicDescription>().apply {
                mSampleRate = format.sampleRate.toDouble()
                mFormatID = kAudioFormatLinearPCM
                mFormatFlags = kLinearPCMFormatFlagIsSignedInteger or kLinearPCMFormatFlagIsPacked
                mBitsPerChannel = BITS_PER_SAMPLE.toUInt()
                mChannelsPerFrame = format.channels.toUInt()
                mFramesPerPacket = 1u
                mBytesPerFrame = (format.channels * BYTES_PER_SAMPLE).toUInt()
                mBytesPerPacket = mBytesPerFrame
            }

            val created = alloc<AudioQueueRefVar>()
            verify(AudioQueueNewInput(description.ptr, inputCallback, owner.asCPointer(), null, null, 0u, created.ptr))

            val audioQueue = created.value ?: throw MicrophoneError.QUEUE_CREATION_FAILED.exception()
            queue = audioQueue

            deviceId?.let { selectDevice(audioQueue, it) }

            repeat(BUFFER_COUNT) {
                val buffer = alloc<AudioQueueBufferRefVar>()
                verify(AudioQueueAllocateBuffer(audioQueue, frameBytes.toUInt(), buffer.ptr))
                verify(AudioQueueEnqueueBuffer(audioQueue, buffer.value, 0u, null))
            }

            verify(AudioQueueStart(audioQueue, null))
        }

        frameSamples
    }.getOrElse { error ->
        close()
        throw error
    }

    override fun close() {
        queue?.let {
            AudioQueueStop(it, true)
            AudioQueueDispose(it, true)
        }
        queue = null

        reference?.dispose()
        reference = null
        slicer = null
    }

    fun receive(buffer: AudioQueueBufferRef) {
        val pointed = buffer.pointed
        val size = pointed.mAudioDataByteSize.toInt()
        val data = pointed.mAudioData ?: return

        if (size > 0) {
            slicer?.write(data.readBytes(size))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun selectDevice(queue: AudioQueueRef, deviceId: String) = memScoped {
    val uid = CFStringCreateWithCString(null, deviceId, kCFStringEncodingUTF8)
        ?: throw MicrophoneError.DEVICE_NOT_FOUND.exception(deviceId)

    try {
        val value = alloc<CFStringRefVar>().apply { this.value = uid }
        verify(
            AudioQueueSetProperty(
                queue, kAudioQueueProperty_CurrentDevice, value.ptr, sizeOf<CFStringRefVar>().convert()
            )
        )
    } finally {
        CFRelease(uid)
    }
}

private fun verify(status: OSStatus) {
    if (status != 0) throw MicrophoneError.CORE_AUDIO_STATUS.exception(status)
}

@OptIn(ExperimentalForeignApi::class)
private val inputCallback = staticCFunction<
        COpaquePointer?,
        AudioQueueRef?,
        AudioQueueBufferRef?,
        CPointer<AudioTimeStamp>?,
        UInt,
        CPointer<AudioStreamPacketDescription>?,
        Unit
        > { userData, queue, buffer, _, _, _ ->
    if (userData != null && buffer != null) {
        userData.asStableRef<AudioQueueMicrophone>().get().receive(buffer)
        AudioQueueEnqueueBuffer(queue, buffer, 0u, null)
    }
}
