package su.plo.lib.mod.client.gui.screen;

import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;

public interface TooltipScreen {

    void setTooltip(@Nullable MinecraftTextComponent tooltip);
}
