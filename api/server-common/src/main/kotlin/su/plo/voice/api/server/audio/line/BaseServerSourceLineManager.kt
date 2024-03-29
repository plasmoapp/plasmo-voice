package su.plo.voice.api.server.audio.line

import su.plo.voice.api.audio.line.SourceLineManager
import java.io.InputStream
import java.util.*

interface BaseServerSourceLineManager<T : BaseServerSourceLine> : SourceLineManager<T> {

    /**
     * @return a new [T] builder
     */
    fun createBuilder(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        weight: Int
    ): BaseServerSourceLine.Builder<T>

    /**
     * @return a new [T] builder
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
