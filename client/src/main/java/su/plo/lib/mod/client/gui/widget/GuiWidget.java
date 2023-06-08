package su.plo.lib.mod.client.gui.widget;

import su.plo.voice.universal.UMatrixStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface GuiWidget {

    ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");

    int getWidth();

    int getHeight();

    void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta);
}
