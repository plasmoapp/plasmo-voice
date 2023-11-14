package su.plo.voice.api.server.audio.provider

import su.plo.voice.api.server.PlasmoBaseVoiceServer
import java.util.concurrent.LinkedBlockingQueue

/**
 * Represents an audio frame provider using a queue implementation.
 *
 * Use this audio frame provider when you need to continuously provide the samples.
 */
class QueueAudioFrameProvider(
    voiceServer: PlasmoBaseVoiceServer,
    stereo: Boolean
) : CollectionAudioFrameProvider(voiceServer, stereo) {

    override val frames = LinkedBlockingQueue<ByteArray>()

    override fun provide20ms(): AudioFrameResult =
        if (encoder.isOpen) {
            val frame = frames.poll()

            if (frame?.size == 0) {
                AudioFrameResult.EndOfStream
            } else {
                AudioFrameResult.Provided(frames.poll())
            }
        }
        else AudioFrameResult.Finished
}
