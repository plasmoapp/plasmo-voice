package su.plo.voice.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;

public enum MicrophoneIconPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public TranslatableText translate() {
        return switch (this) {
            case TOP_LEFT -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_top_left");
            case TOP_CENTER -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_top_center");
            case TOP_RIGHT -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_top_right");
            case BOTTOM_LEFT -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_bottom_left");
            case BOTTOM_CENTER -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_bottom_center");
            case BOTTOM_RIGHT -> new TranslatableText("gui.plasmo_voice.mic_icon_pos_bottom_right");
        };

    }

    public int getX(MinecraftClient client) {
        return switch (this) {
            case TOP_LEFT, BOTTOM_LEFT -> 16;
            case BOTTOM_CENTER, TOP_CENTER -> (client.getWindow().getScaledWidth() / 2) - 8;
            case TOP_RIGHT, BOTTOM_RIGHT -> client.getWindow().getScaledWidth() - 32;
        };
    }

    public int getY(MinecraftClient client) {
        return switch (this) {
            case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> 16;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> client.getWindow().getScaledHeight() - 32;
            case BOTTOM_CENTER -> client.getWindow().getScaledHeight() - 54;
        };
    }
}
