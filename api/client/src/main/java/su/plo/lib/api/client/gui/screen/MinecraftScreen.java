package su.plo.lib.api.client.gui.screen;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

import java.util.List;

public interface MinecraftScreen {

    int getWidth();

    int getHeight();

    void renderBackground();

    void drawTextShadow(@NotNull MinecraftTextComponent text, int x, int y, int color);

    void renderTooltip(List<MinecraftTextComponent> tooltip, int mouseX, int mouseY);
}
