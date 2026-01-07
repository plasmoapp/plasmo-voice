package su.plo.lib.mod.client.gui.screen;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.TooltipData;
import su.plo.slib.api.chat.component.McTextComponent;

public interface TooltipScreen {

    void setTooltip(@Nullable TooltipData tooltip);

    default void setTooltip(@Nullable McTextComponent tooltip, int x, int y) {
        if (tooltip != null) {
            setTooltip(new TooltipData(tooltip, x, y));
        } else {
            setTooltip(null);
        }
    }
}
