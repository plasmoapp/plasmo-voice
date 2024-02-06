package su.plo.voice.addon

import su.plo.voice.api.PlasmoVoice
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.addon.injectPlasmoVoice

@Addon(id = "test", version = "", authors = [])
class TestAddonKt : TestAbstractAddon(), AddonInitializer {

    private val plasmoVoiceDelegate: PlasmoVoice by injectPlasmoVoice()

    @InjectPlasmoVoice
    private lateinit var plasmoVoiceAnnotation: PlasmoVoice

    override fun onAddonInitialize() {
        println("Delegate: $plasmoVoiceDelegate")
        println("Annotation: $plasmoVoiceAnnotation")
        println("Abstract delegate: $plasmoVoiceAbstractDelegate")
        println("Abstract annotation: $plasmoVoiceAbstractDelegate")
    }
}
