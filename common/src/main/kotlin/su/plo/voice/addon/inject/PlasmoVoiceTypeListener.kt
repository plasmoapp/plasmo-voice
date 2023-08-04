package su.plo.voice.addon.inject

import com.google.inject.TypeLiteral
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import su.plo.voice.api.addon.InjectPlasmoVoice

class PlasmoVoiceTypeListener(
    private val type: Class<*>,
    private val instance: Any
) : TypeListener {
    override fun <T : Any> hear(typeLiteral: TypeLiteral<T>, typeEncounter: TypeEncounter<T>) {
        var clazz: Class<*>? = typeLiteral.rawType
        while (clazz != null) {
            clazz.declaredFields
                .filter { it.type == type }
                .filter { it.isAnnotationPresent(InjectPlasmoVoice::class.java) }
                .forEach { typeEncounter.register(PlasmoVoiceMembersListener<T>(it, instance as T)) }

            clazz = clazz.superclass
        }
    }
}
