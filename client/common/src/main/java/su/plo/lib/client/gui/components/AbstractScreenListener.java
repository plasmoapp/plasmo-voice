package su.plo.lib.client.gui.components;

import lombok.Getter;
import lombok.Setter;
import su.plo.lib.client.gui.screen.GuiScreenListener;
import su.plo.lib.client.gui.widget.GuiWidgetListener;

public abstract class AbstractScreenListener implements GuiScreenListener {

    @Getter
    @Setter
    private GuiWidgetListener focused;
    @Getter
    @Setter
    private boolean dragging;
}
