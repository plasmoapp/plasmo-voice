package su.plo.voice.client.gui.widgets;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class BackgroundImageButton extends ImageButton {
    public BackgroundImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, OnPress onPress) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress);
    }

    @Override
    public boolean isHoveredOrFocused() {
        return super.isHoveredOrFocused() && this.active;
    }

    public boolean isHovered(boolean checkActive) {
        return checkActive ? this.visible && this.isHoveredOrFocused() : this.visible && super.isHoveredOrFocused();
    }
}
