package su.plo.voice.client.config.overlay;

import lombok.Getter;
import su.plo.lib.api.chat.MinecraftTextComponent;

public enum OverlayStyle {

    NAME_SKIN("gui.plasmovoice.overlay.style.name_skin"),
    SKIN("gui.plasmovoice.overlay.style.skin"),
    NAME("gui.plasmovoice.overlay.style.name");

    @Getter
    private final MinecraftTextComponent translatable;

    OverlayStyle(String key) {
        this.translatable = MinecraftTextComponent.translatable(key);
    }

    public boolean hasName() {
        return this == NAME_SKIN || this == NAME;
    }

    public boolean hasSkin() {
        return this == NAME_SKIN || this == SKIN;
    }

    public static OverlayStyle fromOrdinal(int ordinal) {
        switch (ordinal) {
            case 1:
                return OverlayStyle.SKIN;
            case 2:
                return OverlayStyle.NAME;
            default:
                return OverlayStyle.NAME_SKIN;
        }
    }
}
