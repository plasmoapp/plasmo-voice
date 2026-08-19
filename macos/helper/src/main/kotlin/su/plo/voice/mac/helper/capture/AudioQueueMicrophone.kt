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
    private var accumulator: FrameAccumulator? = null

    override fun open(format: CaptureFormat, deviceId: String?, onFrame: (ByteArray) -> Unit): Int = runCatching {
        close()

        val frameSamples = format.frameSamples()
        val frameBytes = frameSamples * BYTES_PER_SAMPLE
        accumulator = FrameAccumulator(frameBytes, onFrame)
        val owner = StableRef.create(this@AudioQueueMicrophone).also { reference = it }

        memScoped {
            // This description is the contract we're asking CoreAudio to capture under, it's
            // not negotiated or adjusted by the OS, so every field has to be internally
            // consistent or AudioQueueNewInput below fails outright (or silently produces
            // garbled audio, which, of course, is worse for us).
            //
            // We fix the format to signed 16-bit linear PCM, interleaved and packed (no padding
            // between samples / frames), because that's what FrameAccumulator and the wire
            // protocol both assume.
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

            /*
             * AudioQueueNewInput()
             * https://developer.apple.com/documentation/audiotoolbox/audioqueuenewinput(_:_:_:_:_:_:_:)
             */
            verify(AudioQueueNewInput(description.ptr, inputCallback, owner.asCPointer(), null, null, 0u, created.ptr))

            val audioQueue = created.value ?: throw MicrophoneError.QUEUE_CREATION_FAILED.exception()
            queue = audioQueue

            deviceId?.let { selectDevice(audioQueue, it) }

            // AudioQueue is like a producer / consumer pipeline: at any moment some buffers
            // are "in flight" being filled by the hardware and others are sitting with us,
            // already delivered to inputCallback but not yet handed back.
            //
            // With only one buffer, there'd be a gap between the OS finishing a fill and
            // inputCallback  re-enqueueing it, during that gap CoreAudio has absolutely
            // nowhere to write incoming samples and either drops audio or stalls the input
            // stream. BUFFER_COUNT (3) gives enough slack that inputCallback can lag a buffer
            // or two behind without the line ever running dry.
            repeat(BUFFER_COUNT) {
                val buffer = alloc<AudioQueueBufferRefVar>()

                /*
                 * AudioQueueAllocateBuffer()
                 * https://developer.apple.com/documentation/audiotoolbox/audioqueueallocatebuffer(_:_:_:)
                 */
                verify(AudioQueueAllocateBuffer(audioQueue, frameBytes.toUInt(), buffer.ptr))

                /*
                 * AudioQueueEnqueueBuffer()
                 * https://developer.apple.com/documentation/audiotoolbox/audioqueueenqueuebuffer(_:_:_:_:)
                 */
                verify(AudioQueueEnqueueBuffer(audioQueue, buffer.value, 0u, null))
            }

            /*
             * AudioQueueStart()
             * https://developer.apple.com/documentation/audiotoolbox/audioqueuestart(_:_:)
             */
            verify(AudioQueueStart(audioQueue, null))
        }

        frameSamples
    }.getOrElse { error ->
        close()
        throw error
    }

    override fun close() {
        queue?.let {
            /*
             * AudioQueueStop()
             * https://developer.apple.com/documentation/audiotoolbox/audioqueuestop(_:_:)
             */

            AudioQueueStop(it, true)
            /*
             * AudioQueueDispose()
             * https://developer.apple.com/documentation/audiotoolbox/audioqueuedispose(_:_:)
             */
            AudioQueueDispose(it, true)
        }
        queue = null

        reference?.dispose()
        reference = null
        accumulator = null
    }

    fun receive(buffer: AudioQueueBufferRef) {
        val pointed = buffer.pointed
        val size = pointed.mAudioDataByteSize.toInt()
        val data = pointed.mAudioData ?: return

        if (size > 0) {
            accumulator?.write(data.readBytes(size))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun selectDevice(queue: AudioQueueRef, deviceId: String) = memScoped {
    /*
     * CFStringCreateWithCString()
     * https://developer.apple.com/documentation/corefoundation/cfstringcreatewithcstring(_:_:_:)
     */
    val uid = CFStringCreateWithCString(null, deviceId, kCFStringEncodingUTF8)
        ?: throw MicrophoneError.DEVICE_NOT_FOUND.exception(deviceId)

    try {
        val value = alloc<CFStringRefVar>().apply { this.value = uid }

        /*
         * AudioQueueSetProperty()
         * https://developer.apple.com/documentation/audiotoolbox/audioqueuesetproperty(_:_:_:_:)
         */
        verify(
            AudioQueueSetProperty(
                queue, kAudioQueueProperty_CurrentDevice, value.ptr, sizeOf<CFStringRefVar>().convert()
            )
        )
    } finally {
        /*
         * CFRelease()
         * https://developer.apple.com/documentation/corefoundation/cfrelease
         */
        CFRelease(uid)
    }
}

private fun verify(status: OSStatus) {
    if (status != 0) throw MicrophoneError.CORE_AUDIO_STATUS.exception(status)
}

// The C function pointer AudioQueueNewInput() invokes whenever a buffer fills up with
// captured audio.
//
// Signature mirrors AudioQueueInputCallback, read:
// https://developer.apple.com/documentation/audiotoolbox/audioqueueinputcallback
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

        /*
         * AudioQueueEnqueueBuffer()
         * https://developer.apple.com/documentation/audiotoolbox/audioqueueenqueuebuffer(_:_:_:_:)
         */
        AudioQueueEnqueueBuffer(queue, buffer, 0u, null)
    }
}
