package su.plo.voice.api.client.event.audio.source

import su.plo.voice.api.client.audio.source.ClientAudioSource
import su.plo.voice.api.event.EventCancellableBase

/**
 * This event is fired when the [ClientAudioSource] is about to reset.
 *
 * Reset sets [ClientAudioSource.isActivated] and [ClientAudioSource.canHear] to false
 * and resets the decoder used for this source.
 */
data class AudioSourceResetEvent(
    val source: ClientAudioSource<*>,
    val cause: Cause
) : EventCancellableBase() {

    enum class Cause {

        /**
         * When the underlying source is stopped.
         */
        SOURCE_STOPPED,

        /**
         * When the voice end packet was not received and [ClientAudioSource.closeTimeoutMs] is reached.
         */
        TIMED_OUT,

        /**
         * When the voice end packet received.
         */
        VOICE_END
    }
}
