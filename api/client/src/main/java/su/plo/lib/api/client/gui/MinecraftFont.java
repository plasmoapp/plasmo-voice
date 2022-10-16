package su.plo.lib.api.client.gui;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;

public interface MinecraftFont {

    int getLineHeight();

    int width(@NotNull String text);

    int width(@NotNull TextComponent text);

    @NotNull String plainSubstrByWidth(String string, int width, boolean tail);

    @NotNull String plainSubstrByWidth(String string, int width);
}
