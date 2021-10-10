package su.plo.voice.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;

public enum MicrophoneIconPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public TranslatableComponent translate() {
        return switch (this) {
            case TOP_LEFT -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_left");
            case TOP_CENTER -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_center");
            case TOP_RIGHT -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_right");
            case BOTTOM_LEFT -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_left");
            case BOTTOM_CENTER -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_center");
            case BOTTOM_RIGHT -> new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_right");
        };

    }

    public int getX(Minecraft client) {
        return switch (this) {
            case TOP_LEFT, BOTTOM_LEFT -> 16;
            case BOTTOM_CENTER, TOP_CENTER -> (client.getWindow().getGuiScaledWidth() / 2) - 8;
            case TOP_RIGHT, BOTTOM_RIGHT -> client.getWindow().getGuiScaledWidth() - 32;
        };
    }

    public int getY(Minecraft client) {
        return switch (this) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 16;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> client.getWindow().getGuiScaledHeight() - 32;
            case BOTTOM_CENTER -> client.getWindow().getGuiScaledHeight() - 54;
        };
    }
}
