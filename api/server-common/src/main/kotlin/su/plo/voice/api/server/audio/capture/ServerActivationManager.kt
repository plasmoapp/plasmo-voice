package su.plo.voice.api.server.audio.capture

import su.plo.voice.api.audio.capture.ActivationManager
import java.io.InputStream
import java.util.*

/**
 * Manages server activations.
 */
interface ServerActivationManager : ActivationManager<ServerActivation> {

    /**
     * Creates a new activation builder for building server activations.
     *
     * @param addonObject The addon associated with the activation.
     * @param name The name of the activation.
     * @param translation The translation key for the activation.
     * @param icon The icon representation of the source line can be a Minecraft ResourceLocation or a base64-encoded string.
     * @param permission The permission required for the activation.
     * @param weight The weight of the activation.
     * @return A new builder for server activations.
     */
    fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        permission: String,
        weight: Int
    ): ServerActivation.Builder

    /**
     * Creates a new activation builder for building server activations.
     *
     * @param addonObject The addon associated with the activation.
     * @param name The name of the activation.
     * @param translation The translation key for the activation.
     * @param icon An InputStream representing the icon image.
     * @param permission The permission required for the activation.
     * @param weight The weight of the activation.
     * @return A new builder for server activations.
     */
    fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: InputStream,
        permission: String,
        weight: Int
    ): ServerActivation.Builder {
        val base64 = Base64.getEncoder().encodeToString(icon.readBytes())

        return createBuilder(
            addonObject,
            name,
            translation,
            "base64;$base64",
            permission,
            weight
        )
    }
}
