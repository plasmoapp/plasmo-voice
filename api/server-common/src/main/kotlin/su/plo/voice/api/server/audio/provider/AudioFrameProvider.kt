package su.plo.voice.api.server.audio.provider

/**
 * Represents an interface for providing encoded & encrypted audio frames.
 */
interface AudioFrameProvider {

    /**
     * Provides the 20ms encoded & encrypted audio frame.
     *
     * @see AudioFrameResult
     */
    fun provide20ms(): AudioFrameResult
}
