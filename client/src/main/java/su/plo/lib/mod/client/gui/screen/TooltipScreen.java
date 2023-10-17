package su.plo.lib.mod.client.gui.screen;

import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.chat.component.McTextComponent;

public interface TooltipScreen {

    void setTooltip(@Nullable McTextComponent tooltip);
}
