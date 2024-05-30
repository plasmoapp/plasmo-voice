package su.plo.lib.mod.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface GuiWidget {

    //#if MC>=12005
    //$$ ResourceLocation MENU_LIST_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/menu_list_background.png");
    //$$ ResourceLocation INWORLD_MENU_LIST_BACKGROUND_LOCATION = new ResourceLocation("textures/gui/inworld_menu_list_background.png");
    //$$
    //$$ ResourceLocation FOOTER_SEPARATOR_LOCATION = new ResourceLocation("textures/gui/footer_separator.png");
    //$$ ResourceLocation INWORLD_FOOTER_SEPARATOR_LOCATION = new ResourceLocation("textures/gui/inworld_footer_separator.png");
    //#else
    ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");
    //#endif

    int getWidth();

    int getHeight();

    void render(@NotNull PoseStack stack, int mouseX, int mouseY, float delta);
}
