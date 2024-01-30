package su.plo.voice.api.server.audio.provider

sealed class AudioFrameResult {

    /**
     * 20ms encoded & encrypted audio frame or null.
     */
    class Provided(val frame: ByteArray?) : AudioFrameResult()

    /**
     * [EndOfStream] means that [AudioFrameProvider] reaches the end of the current stream,
     * but not completely closed.
     */
    object EndOfStream : AudioFrameResult()

    /**
     * [Finished] means that [AudioFrameProvider] is completely closed and will not return any frames.
     */
    object Finished : AudioFrameResult()
}
