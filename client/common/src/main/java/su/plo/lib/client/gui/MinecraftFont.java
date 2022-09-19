package su.plo.lib.client.gui;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.TextComponent;

public interface MinecraftFont {

    int getLineHeight();

    int width(@NotNull String text);

    int width(@NotNull TextComponent text);

    @NotNull String plainSubstrByWidth(String string, int width, boolean tail);

    @NotNull String plainSubstrByWidth(String string, int width);
}
