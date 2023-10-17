package su.plo.voice.api.server.audio.line

import su.plo.voice.api.audio.line.SourceLineManager
import java.io.InputStream
import java.util.*

/**
 * Base interface for managing server audio source lines.
 */
interface BaseServerSourceLineManager<T : BaseServerSourceLine> : SourceLineManager<T> {

    /**
     * Creates a new builder for building server audio source lines.
     *
     * @param addonObject The addon associated with the source line.
     * @param name The name of the source line.
     * @param translation The translation key for the source line.
     * @param icon The icon representation of the source line can be a Minecraft ResourceLocation or a base64-encoded string.
     * @param weight The weight of the source line.
     * @return A new builder for server audio source line.
     */
    fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        weight: Int
    ): BaseServerSourceLine.Builder<T>

    /**
     * Creates a new builder for building instances of server audio source lines
     * using an InputStream as the icon source.
     * The icon will be encoded as a base64 string.
     *
     * @param addonObject The addon associated with the source line.
     * @param name The name of the source line.
     * @param translation The translation key for the source line.
     * @param icon An InputStream representing the icon image.
     * @param weight The weight of the source line.
     * @return A new builder for server audio source line.
     */
    fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: InputStream,
        weight: Int
    ): BaseServerSourceLine.Builder<T> {
        val base64 = Base64.getEncoder().encodeToString(icon.readBytes())

        return createBuilder(
            addonObject,
            name,
            translation,
            "base64;$base64",
            weight
        )
    }
}
