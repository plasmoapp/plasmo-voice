package su.plo.voice.api.context

import su.plo.voice.api.PlasmoVoice

/**
 * An interface that holds a Plasmo Voice instance.
 */
interface PlasmoVoiceHolder<T : PlasmoVoice> {

    /**
     * The instance of Plasmo Voice.
     */
    val voiceInstance: T
}
