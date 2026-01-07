package su.plo.lib.mod.client.gui.components;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.screen.GuiScreenListener;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;

public abstract class AbstractScreenListener implements GuiScreenListener {

    @Getter
    private GuiWidgetListener focused;
    @Getter
    @Setter
    private boolean dragging;

    @Override
    public void applyFocus(boolean focused) {
        if (!focused && this.focused != null) {
            this.focused.applyFocus(false);
            this.focused = null;
        }
    }

    @Override
    public void setFocused(@Nullable GuiWidgetListener focused) {
        if (this.focused == focused) return;

        if (this.focused != null) {
            this.focused.applyFocus(false);
        }
        this.focused = focused;
        if (focused != null) {
            focused.applyFocus(true);
        }
    }
}
