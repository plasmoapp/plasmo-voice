package su.plo.voice.api.client.config.overlay

import su.plo.slib.api.chat.component.McTextComponent

/**
 * Overlay style.
 */
enum class OverlayStyle(key: String) {

    NAME_SKIN("gui.plasmovoice.overlay.style.name_skin"),
    SKIN("gui.plasmovoice.overlay.style.skin"),
    NAME("gui.plasmovoice.overlay.style.name");

    val translatable = McTextComponent.translatable(key)

    val hasName: Boolean
        get() = this == NAME_SKIN || this == NAME

    val hasSkin: Boolean
        get() = this == NAME_SKIN || this == SKIN

    companion object {

        fun fromOrdinal(ordinal: Int): OverlayStyle {
            return when (ordinal) {
                1 -> SKIN
                2 -> NAME
                else -> NAME_SKIN
            }
        }
    }
}
