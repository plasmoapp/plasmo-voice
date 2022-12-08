package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import su.plo.voice.client.gui.VoiceSettingsScreen;

import java.util.List;
import java.util.function.Supplier;

public class TooltipImageButton extends ImageButton {

    private final VoiceSettingsScreen parent;
    private final Supplier<List<Component>> tooltipSupplier;

    public TooltipImageButton(VoiceSettingsScreen parent,
                              int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation,
                              int p, int q,
                              OnPress onPress,
                              Supplier<List<Component>> tooltipSupplier) {
        super(i, j, k, l, m, n, o, resourceLocation, p, q, onPress);

        this.parent = parent;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        super.renderButton(poseStack, i, j, f);
        if (this.isHoveredOrFocused()) {
            parent.setTooltip(tooltipSupplier.get());
        }
    }
}
