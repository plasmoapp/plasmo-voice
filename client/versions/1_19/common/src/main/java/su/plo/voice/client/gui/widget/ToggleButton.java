package su.plo.voice.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;

public final class ToggleButton extends AbstractWidget implements UpdatableWidget {

    private final @Nullable PressAction action;
    private final ConfigEntry<Boolean> entry;

    public ToggleButton(int x, int y, int width, int height, ConfigEntry<Boolean> entry) {
        this(x, y, width, height, entry, null);
    }

    public ToggleButton(int x, int y, int width, int height, ConfigEntry<Boolean> entry, @Nullable PressAction action) {
        super(x, y, width, height, entry.value() ? Component.translatable("gui.plasmovoice.on") : Component.translatable("gui.plasmovoice.off"));

        this.entry = entry;
        this.action = action;
    }

    public void invertToggle() {
        entry.set(!entry.value());
        setMessage(getText());

        if (action != null) action.onToggle(entry.value());
    }

    @Override
    public void updateValue() {
        setMessage(getText());
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        invertToggle();
    }

    @Override
    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int i = (isHoveredOrFocused() && active ? 2 : 1) * 20;
        if (entry.value()) {
            blit(poseStack, x + (int)((double)(width - 9)), y, 0, 46 + i, 4, 20);
            blit(poseStack, x + (int)((double)(width - 9)) + 4, y, 196, 46 + i, 4, 20);
        } else {
            blit(poseStack, x, y, 0, 46 + i, 4, 20);
            blit(poseStack, x + 4, y, 196, 46 + i, 4, 20);
        }
    }

    private Component getText() {
        return entry.value() ? Component.translatable("gui.plasmovoice.on") : Component.translatable("gui.plasmovoice.off");
    }

    public interface PressAction {

        void onToggle(boolean toggled);
    }
}
