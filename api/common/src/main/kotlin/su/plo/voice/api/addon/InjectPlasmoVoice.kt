package su.plo.voice.api.addon

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
/**
 * This annotation is used by addons to request injection of the
 * [PlasmoVoiceServer], [PlasmoVoiceProxy] or [PlasmoVoiceClient] instance.
 *
 * Example usage:
 *
 * kotlin:
 * ```kotlin
 * class MyAddon {
 *     @InjectPlasmoVoice
 *     lateinit var voiceServer: PlasmoVoiceServer
 * }
 * ```
 *
 * java:
 * ```java
 * class MyAddon {
 *     @InjectPlasmoVoice
 *     private final PlasmoVoiceServer voiceServer;
 * }
 * ```
 *
 * The [PlasmoVoice] instance will be automatically injected into the annotated field
 * when the addon is initialized.
 */
annotation class InjectPlasmoVoice
