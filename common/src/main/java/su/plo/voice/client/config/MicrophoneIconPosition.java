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
        switch (this) {
            case TOP_LEFT:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_left");
            case TOP_CENTER:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_center");
            case TOP_RIGHT:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.top_right");
            case BOTTOM_LEFT:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_left");
            case BOTTOM_CENTER:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_center");
            case BOTTOM_RIGHT:
                return new TranslatableComponent("gui.plasmo_voice.general.icons.position.bottom_right");
            default:
                throw new IllegalArgumentException();
        }

    }

    public int getX(Minecraft client) {
        switch (this) {
            case TOP_LEFT:
            case BOTTOM_LEFT:
                return 16;
            case BOTTOM_CENTER:
            case TOP_CENTER:
                return (client.getWindow().getGuiScaledWidth() / 2) - 8;
            case TOP_RIGHT:
            case BOTTOM_RIGHT:
                return client.getWindow().getGuiScaledWidth() - 32;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getY(Minecraft client) {
        switch (this) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                return 16;
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                return client.getWindow().getGuiScaledHeight() - 32;
            case BOTTOM_CENTER:
                return client.getWindow().getGuiScaledHeight() - 54;
            default:
                throw new IllegalArgumentException();
        }
    }
}
