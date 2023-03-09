package su.plo.voice.api.server.audio.line

import su.plo.voice.api.audio.line.SourceLineManager

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
}
