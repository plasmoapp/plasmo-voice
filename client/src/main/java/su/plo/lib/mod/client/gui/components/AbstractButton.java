package su.plo.lib.mod.client.gui.components;

import com.mojang.blaze3d.platform.InputConstants;
import su.plo.slib.api.chat.component.McTextComponent;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;

public abstract class AbstractButton extends GuiAbstractWidget {

    public AbstractButton(
            int x,
            int y,
            int width,
            int height,
            @NotNull McTextComponent text
    ) {
        super(x, y, width, height, text);
    }

    // GuiAbstractWidget impl
    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress();
    }

    // GuiWidgetListener impl
    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (!active || !visible) return false;

        if (keyCode != InputConstants.KEY_RETURN && keyCode != InputConstants.KEY_SPACE && keyCode != InputConstants.KEY_NUMPADENTER) return false;

        playDownSound();
        onPress();
        return true;
    }

    public abstract void onPress();
}
