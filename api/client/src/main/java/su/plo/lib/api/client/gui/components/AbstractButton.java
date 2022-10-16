package su.plo.lib.api.client.gui.components;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.widget.GuiAbstractWidget;

public abstract class AbstractButton extends GuiAbstractWidget {

    public AbstractButton(@NotNull MinecraftClientLib minecraft,
                          int x,
                          int y,
                          int width,
                          int height,
                          @NotNull TextComponent text) {
        super(minecraft, x, y, width, height, text);
    }

    // GuiAbstractWidget impl
    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress();
    }

    // GuiWidgetListener impl
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!active || !visible) return false;

        // GLFW_KEY_ENTER && GLFW_KEY_SPACE && GLFW_KEY_KP_ENTER
        if (keyCode != 257 && keyCode != 32 && keyCode != 335) return false;

        playDownSound();
        onPress();
        return true;
    }

    public abstract void onPress();
}
