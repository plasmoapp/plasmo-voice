package su.plo.voice.addon

import su.plo.voice.api.PlasmoVoice
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.injectPlasmoVoice

abstract class TestAbstractAddon {

    val plasmoVoiceAbstractDelegate: PlasmoVoice by injectPlasmoVoice()

    @InjectPlasmoVoice
    lateinit var plasmoVoiceAbstractAnnotation: PlasmoVoice
}
