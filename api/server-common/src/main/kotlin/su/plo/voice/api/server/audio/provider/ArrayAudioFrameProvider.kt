package su.plo.voice.api.server.audio.provider

import su.plo.voice.api.server.PlasmoBaseVoiceServer

/**
 * Represents an audio frame provider using an array implementation.
 *
 * Use this audio frame provider when you need to provide a fixed set of frames.
 *
 * If frames are not looped using [loop], when the end of the list is reached,
 * the provider will return [AudioFrameResult.Finished].
 */
class ArrayAudioFrameProvider(
    voiceServer: PlasmoBaseVoiceServer,
    stereo: Boolean
) : CollectionAudioFrameProvider(voiceServer, stereo) {

    /**
     * Set whether the frames in the array should loop.
     *
     * @return Whether the frames are looped.
     */
    var loop = false

    override val frames = ArrayList<ByteArray>()

    private var index = 0

    override fun provide20ms(): AudioFrameResult =
        if (frames.isEmpty() || (frames.size - 1 == index && !loop)) {
            AudioFrameResult.Finished
        } else {
            AudioFrameResult.Provided(frames[index]).also {
                if (loop) {
                    this.index = (index + 1) % frames.size
                } else {
                    this.index++
                }
            }
        }
}
