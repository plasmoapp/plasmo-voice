package su.plo.voice.addon.inject

import su.plo.voice.api.PlasmoVoice
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.InjectPlasmoVoiceDelegate

fun injectPlasmoVoiceInto(voiceInstance: PlasmoVoice, target: Any) {
    injectAnnotation(voiceInstance, target)
    injectDelegate(voiceInstance, target)
}

private fun injectAnnotation(voiceInstance: PlasmoVoice, target: Any) {
    generateSequence(target.javaClass) { it.superclass }
        .forEach { targetClass ->
            targetClass.declaredFields
                .filter { PlasmoVoice::class.java.isAssignableFrom(it.type) }
                .filter { it.isAnnotationPresent(InjectPlasmoVoice::class.java) }
                .forEach {
                    it.isAccessible = true
                    it.set(target, voiceInstance)
                }
        }
}

private fun injectDelegate(voiceInstance: PlasmoVoice, target: Any) {
    generateSequence(target.javaClass) { it.superclass }
        .forEach { targetClass ->
            targetClass.declaredFields
                .forEach {
                    it.isAccessible = true
                    val fieldValue = it.get(target)
                    if (fieldValue !is InjectPlasmoVoiceDelegate<*>) return@forEach

                    injectAnnotation(voiceInstance, fieldValue)
                }
        }
}
