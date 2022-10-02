package su.plo.lib.client.gui.screen;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;

import java.util.List;

public interface MinecraftScreen {

    int getWidth();

    int getHeight();

    void renderBackground();

    void drawTextShadow(@NotNull TextComponent text, int x, int y, int color);

    void renderTooltip(List<TextComponent> tooltip, int mouseX, int mouseY);
}
