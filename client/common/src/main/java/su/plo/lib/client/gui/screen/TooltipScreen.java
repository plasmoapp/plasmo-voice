package su.plo.lib.client.gui.screen;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.chat.TextComponent;

import java.util.List;

public interface TooltipScreen {

    void setTooltip(@NotNull List<TextComponent> tooltip);
}
