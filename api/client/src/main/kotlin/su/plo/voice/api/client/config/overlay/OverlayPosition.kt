package su.plo.voice.api.client.config.overlay

enum class OverlayPosition(
    val x: Int,
    val y: Int,
    val translation: String
) {

    TOP_LEFT(4, 4, "gui.plasmovoice.overlay.hud_position.top_left"),
    TOP_RIGHT(-4, 4, "gui.plasmovoice.overlay.hud_position.top_right"),
    BOTTOM_LEFT(4, -4, "gui.plasmovoice.overlay.hud_position.bottom_left"),
    BOTTOM_RIGHT(-4, -4, "gui.plasmovoice.overlay.hud_position.bottom_right");

    val isRight: Boolean
        get() = this == BOTTOM_RIGHT || this == TOP_RIGHT

    val isBottom: Boolean
        get() = this == BOTTOM_LEFT || this == BOTTOM_RIGHT
}
