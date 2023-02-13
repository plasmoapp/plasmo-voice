package su.plo.voice.client.config.overlay;

import lombok.Getter;

public enum OverlayPosition {

    TOP_LEFT(4, 4, "gui.plasmovoice.overlay.hud_position.top_left"),
    TOP_RIGHT(-4, 4, "gui.plasmovoice.overlay.hud_position.top_right"),
    BOTTOM_LEFT(4, -4, "gui.plasmovoice.overlay.hud_position.bottom_left"),
    BOTTOM_RIGHT(-4, -4, "gui.plasmovoice.overlay.hud_position.bottom_right");

    @Getter
    private final Integer x;
    @Getter
    private final Integer y;
    @Getter
    private final String translation;

    OverlayPosition(Integer x, Integer y, String translation) {
        this.x = x;
        this.y = y;
        this.translation = translation;
    }

    public boolean isRight() {
        return this == BOTTOM_RIGHT || this == TOP_RIGHT;
    }

    public boolean isBottom() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }
}
