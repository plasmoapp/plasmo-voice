package su.plo.voice.api.addon

import su.plo.voice.api.PlasmoVoice
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class InjectPlasmoVoiceDelegate<T : PlasmoVoice> : ReadOnlyProperty<Any, T> {

    @InjectPlasmoVoice
    private var value: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T =
        value ?: throw IllegalStateException("Plasmo Voice is not initialized yet")
}


fun <T : PlasmoVoice> injectPlasmoVoice(): ReadOnlyProperty<Any, T> =
    InjectPlasmoVoiceDelegate()
