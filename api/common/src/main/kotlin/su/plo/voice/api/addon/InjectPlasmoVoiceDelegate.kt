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

/**
 * Delegate for injecting Plasmo Voice.
 *
 * The [PlasmoVoice] instance will be automatically injected into the delegate when the addon is initialized.
 **
 * @throws IllegalStateException If [PlasmoVoice] is not initialized yet
 */
fun <T : PlasmoVoice> injectPlasmoVoice(): ReadOnlyProperty<Any, T> =
    InjectPlasmoVoiceDelegate()
