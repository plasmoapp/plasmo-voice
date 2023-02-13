package su.plo.lib.mod.client.gui.components;

import gg.essential.universal.UKeyboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;

public abstract class AbstractButton extends GuiAbstractWidget {

    public AbstractButton(int x,
                          int y,
                          int width,
                          int height,
                          @NotNull MinecraftTextComponent text) {
        super(x, y, width, height, text);
    }

    // GuiAbstractWidget impl
    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress();
    }

    // GuiWidgetListener impl
    @Override
    public boolean keyPressed(int keyCode, char typedChar, UKeyboard.@Nullable Modifiers modifiers) {
        if (!active || !visible) return false;

        // GLFW_KEY_ENTER && GLFW_KEY_SPACE && GLFW_KEY_KP_ENTER
        if (keyCode != 257 && keyCode != 32 && keyCode != 335) return false;

        playDownSound();
        onPress();
        return true;
    }

    public abstract void onPress();
}
