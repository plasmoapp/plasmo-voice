package su.plo.voice.client.config;

import lombok.Getter;

public enum IconPosition {

    TOP_LEFT(16, 16, "gui.plasmovoice.overlay.hud_position.top_left"),
    TOP_CENTER(null, 16, "gui.plasmovoice.overlay.hud_position.top_center"),
    TOP_RIGHT(-16, 16, "gui.plasmovoice.overlay.hud_position.top_right"),
    BOTTOM_LEFT(16, -16, "gui.plasmovoice.overlay.hud_position.bottom_left"),
    BOTTOM_CENTER(null, -38, "gui.plasmovoice.overlay.hud_position.bottom_center"),
    BOTTOM_RIGHT(-16, -16, "gui.plasmovoice.overlay.hud_position.bottom_right");

    @Getter
    private final Integer x;
    @Getter
    private final Integer y;
    @Getter
    private final String translation;

    IconPosition(Integer x, Integer y, String translation) {
        this.x = x;
        this.y = y;
        this.translation = translation;
    }
}
