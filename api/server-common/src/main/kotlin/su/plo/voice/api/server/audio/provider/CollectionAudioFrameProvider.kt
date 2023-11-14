package su.plo.voice.api.server.audio.provider

import su.plo.voice.api.server.PlasmoBaseVoiceServer

/**
 * Represents an abstract audio frame provider using collection.
 */
abstract class CollectionAudioFrameProvider(
    voiceServer: PlasmoBaseVoiceServer,
    stereo: Boolean
) : AudioFrameProvider, AutoCloseable {

    // 20ms 48kHz frame
    protected val frameSize =
        if (stereo) 960 * 2
        else 960

    protected val encryption = voiceServer.defaultEncryption
    protected val encoder = voiceServer.createOpusEncoder(stereo)

    protected abstract val frames: MutableCollection<ByteArray>

    /**
     * Adds an 48kHz 16-bit PCM audio samples to the queue.
     */
    fun addSamples(samples: ShortArray) {
        if (!encoder.isOpen) throw IllegalStateException("Frame provider is closed")

        samples.splitFrames(frameSize)
            .forEach {
                addEncodedFrame(encoder.encode(it))
            }
        addEnd()
    }

    /**
     * Adds an Opus encoded audio samples to the queue.
     */
    fun addEncodedFrame(frame: ByteArray) {
        if (!encoder.isOpen) throw IllegalStateException("Frame provider is closed")

        val encrypted = encryption.encrypt(frame)
        frames.add(encrypted)
    }

    /**
     * Adds an end of the stream marker to the queue.
     */
    fun addEnd() {
        if (!encoder.isOpen) throw IllegalStateException("Frame provider is closed")

        frames.add(ByteArray(0))
        encoder.reset()
    }

    /**
     * Removes all frames.
     */
    fun clear() {
        frames.clear()
    }

    /**
     * Closes the encoder and frame provider.
     *
     * Next calls of [provide20ms] will return [AudioFrameResult.Finished].
     */
    final override fun close() {
        encoder.close()
    }

    protected fun ShortArray.splitFrames(frameSize: Int): Iterator<ShortArray> =
        object : AbstractIterator<ShortArray>() {

            private var index = 0

            override fun computeNext() {
                val samples = this@splitFrames

                if (index >= samples.size || (index + frameSize) > samples.size) {
                    done()
                    return
                }

                val frame = samples.copyOfRange(index, index + frameSize)
                setNext(frame)

                index += frameSize
            }
        }
}
