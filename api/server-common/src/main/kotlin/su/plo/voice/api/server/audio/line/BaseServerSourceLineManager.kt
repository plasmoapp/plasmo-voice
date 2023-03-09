package su.plo.voice.api.server.audio.line

import su.plo.voice.api.audio.line.SourceLineManager

interface BaseServerSourceLineManager<T : BaseServerSourceLine> : SourceLineManager<T> {

    /**
     * @return a new [T] if source line with specified name doesn't exist;
     * otherwise returns registered source line
     */
    fun register(
        addonObject: Any,
        name: String,
        translation: String,
        icon: String,
        weight: Int,
        withPlayers: Boolean = false,
        defaultVolume: Double = 1.0
    ): T
}
