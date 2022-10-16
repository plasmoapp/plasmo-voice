package su.plo.lib.mod.client.gui;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.chat.TextConverter;
import su.plo.lib.api.client.gui.MinecraftFont;

@RequiredArgsConstructor
public final class ModFontWrapper implements MinecraftFont {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private final TextConverter<Component> textConverter;

    @Override
    public int getLineHeight() {
        return minecraft.font.lineHeight;
    }

    @Override
    public int width(@NotNull String text) {
        return minecraft.font.width(text);
    }

    @Override
    public int width(@NotNull TextComponent text) {
        return minecraft.font.width(textConverter.convert(text));
    }

    @Override
    public @NotNull String plainSubstrByWidth(String string, int width, boolean tail) {
        return minecraft.font.plainSubstrByWidth(string, width, tail);
    }

    @Override
    public @NotNull String plainSubstrByWidth(String string, int width) {
        return minecraft.font.plainSubstrByWidth(string, width);
    }
}
