package su.plo.voice.api.client.audio.line

import su.plo.slib.api.chat.component.McTextComponent
import su.plo.voice.proto.data.audio.line.SourceLine

/**
 * Represents a client source line.
 */
interface ClientSourceLine : SourceLine, ClientPlayerSet {

    /**
     * Gets or sets the volume for this source line.
     *
     * @return The volume of this source line.
     */
    var volume: Double

    /**
     * Gets the last time an audio source on this line was activated.
     *
     * In-memory and per-session: it resets to `0` when the client reconnects.
     *
     * @return The last activation time in epoch milliseconds.
     */
    val lastActivationTime: Long

    /**
     * Gets the translation component associated with this source line.
     *
     * @return The translation component.
     */
    val translationComponent: McTextComponent
        get() = McTextComponent.translatable(translation)
}
