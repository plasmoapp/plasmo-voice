package su.plo.voice.api.client.config

enum class IconPosition(
    val x: Int?,
    val y: Int?,
    val translation: String
) {
    TOP_LEFT(16, 16, "gui.plasmovoice.overlay.hud_position.top_left"),
    TOP_CENTER(null, 16, "gui.plasmovoice.overlay.hud_position.top_center"),
    TOP_RIGHT(-16, 16, "gui.plasmovoice.overlay.hud_position.top_right"),
    BOTTOM_LEFT(16, -16, "gui.plasmovoice.overlay.hud_position.bottom_left"),
    BOTTOM_CENTER(null, -38, "gui.plasmovoice.overlay.hud_position.bottom_center"),
    BOTTOM_RIGHT(-16, -16, "gui.plasmovoice.overlay.hud_position.bottom_right")
}
