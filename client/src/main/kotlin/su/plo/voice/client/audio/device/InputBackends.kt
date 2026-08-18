package su.plo.voice.client.audio.device

import su.plo.voice.api.client.audio.device.DeviceFactory
import su.plo.voice.api.client.audio.device.DeviceFactoryManager
import su.plo.voice.api.client.config.ClientConfig
import su.plo.voice.api.client.config.InputBackend
import su.plo.voice.util.isMac

/**
 * Turns the configured backend into the factories to try, best first.
 *
 * Everything after the first entry is a fallback, so a machine whose preferred backend is missing
 * or broken still ends up with a working microphone.
 */
fun ClientConfig.Voice.inputBackends(): List<InputBackend> {
    migrateJavaxInput()

    val preferred = inputBackend.value()
    val fallbacks = if (isMac()) listOf(InputBackend.COREAUDIO, InputBackend.JAVAX) else listOf(InputBackend.OPEN_AL, InputBackend.JAVAX)

    return (listOf(preferred) + fallbacks).filter { it != InputBackend.AUTO }.distinct()
}

/** The factory the player's choice points at. */
fun DeviceFactoryManager.inputFactory(config: ClientConfig.Voice): DeviceFactory =
    config.inputBackends().firstNotNullOfOrNull { getDeviceFactory(it.factoryName).orElse(null) }
        ?: throw IllegalStateException("No input device factory is registered")

private fun ClientConfig.Voice.migrateJavaxInput() {
    @Suppress("DEPRECATION")
    if (useJavaxInput.value() && inputBackend.value() == InputBackend.AUTO) {
        inputBackend.set(InputBackend.JAVAX)
    }
}
