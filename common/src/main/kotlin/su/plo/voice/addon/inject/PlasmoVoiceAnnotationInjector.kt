package su.plo.voice.addon.inject

import com.google.inject.MembersInjector
import java.lang.reflect.Field

class PlasmoVoiceAnnotationInjector<T>(
    private val field: Field,
    private val instance: T
) : MembersInjector<T> {

    init {
        field.isAccessible = true
    }

    override fun injectMembers(obj: T) {
        field.set(obj, instance)
    }
}
