package su.plo.lib.client.gui.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.gui.GuiRender;

public interface GuiWidget {

    String WIDGETS_LOCATION = "textures/gui/widgets.png";
    String BACKGROUND_LOCATION = "textures/gui/options_background.png";

    int getWidth();

    int getHeight();

    void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta);
}
