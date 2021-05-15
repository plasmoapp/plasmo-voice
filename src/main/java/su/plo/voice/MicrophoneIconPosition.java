package su.plo.voice;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;

public enum MicrophoneIconPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    public TranslationTextComponent translate() {
        switch (this) {
            case TOP_LEFT:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_top_left");
            case TOP_CENTER:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_top_center");
            case TOP_RIGHT:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_top_right");
            case BOTTOM_LEFT:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_bottom_left");
            case BOTTOM_CENTER:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_bottom_center");
            case BOTTOM_RIGHT:
                return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_bottom_right");
        }

        return new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_bottom_center");
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
        }

        return (client.getWindow().getGuiScaledWidth() / 2) - 8;
    }

    public int getY(Minecraft client) {
        switch (this) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                return 16;
            case BOTTOM_LEFT:
                return client.getWindow().getGuiScaledHeight() - 32;
            case BOTTOM_CENTER:
                return client.getWindow().getGuiScaledHeight() - 54;
            case BOTTOM_RIGHT:
                return client.getWindow().getGuiScaledHeight() - 32;
        }

        return client.getWindow().getGuiScaledHeight() - 54;
    }
}
