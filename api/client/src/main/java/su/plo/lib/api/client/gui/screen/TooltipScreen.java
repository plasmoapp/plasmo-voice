package su.plo.lib.api.client.gui.screen;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;

import java.util.List;

public interface TooltipScreen {

    void setTooltip(@NotNull List<MinecraftTextComponent> tooltip);
}
