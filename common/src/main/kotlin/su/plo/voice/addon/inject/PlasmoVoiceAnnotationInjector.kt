package su.plo.voice.addon.inject

import su.plo.voice.api.PlasmoVoice
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.InjectPlasmoVoiceDelegate

class PlasmoVoiceAnnotationInjector<T : PlasmoVoice>(
    private val instance: T
) {

    fun inject(obj: Any) {
        injectAnnotation(obj)
        injectDelegate(obj)
    }

    fun injectAnnotation(obj: Any) {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            clazz.declaredFields
                .filter { PlasmoVoice::class.java.isAssignableFrom(it.type) }
                .filter { it.isAnnotationPresent(InjectPlasmoVoice::class.java) }
                .forEach {
                    it.isAccessible = true
                    it.set(obj, instance)
                }

            clazz = clazz.superclass
        }
    }

    fun injectDelegate(obj: Any) {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            clazz.declaredFields
                .forEach {
                    it.isAccessible = true
                    val fieldValue = it.get(obj)
                    if (fieldValue !is InjectPlasmoVoiceDelegate<*>) return@forEach

                    injectAnnotation(fieldValue)
                }

            clazz = clazz.superclass
        }
    }
}
