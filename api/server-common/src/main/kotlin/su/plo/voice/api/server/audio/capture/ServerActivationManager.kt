package su.plo.voice.api.server.audio.capture

import su.plo.voice.api.audio.capture.ActivationManager
import java.io.InputStream
import java.util.*

// todo: doc
interface ServerActivationManager : ActivationManager<ServerActivation> {
    /**
     * Creates a new activation builder
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
     * Creates a new activation builder
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
