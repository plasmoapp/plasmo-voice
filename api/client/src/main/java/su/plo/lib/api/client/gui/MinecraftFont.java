package su.plo.lib.api.client.gui;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

public interface MinecraftFont {

    int getLineHeight();

    int width(@NotNull String text);

    int width(@NotNull MinecraftTextComponent text);

    @NotNull String plainSubstrByWidth(String string, int width, boolean tail);

    @NotNull String plainSubstrByWidth(String string, int width);
}
