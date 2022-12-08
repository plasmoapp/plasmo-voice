package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import su.plo.voice.client.config.entries.BooleanConfigEntry;

public class ToggleButton extends AbstractWidget {
    private final PressAction action;
    private final BooleanConfigEntry toggled;

    public ToggleButton(int x, int y, int width, int height, BooleanConfigEntry toggled, PressAction action) {
        super(x, y, width, height, toggled.get() ? Component.translatable("gui.plasmo_voice.on") : Component.translatable("gui.plasmo_voice.off"));
        this.toggled = toggled;
        this.action = action;
    }

    public void updateValue() {
        this.setMessage(getText());
    }

    private Component getText() {
        return toggled.get() ? Component.translatable("gui.plasmo_voice.on") : Component.translatable("gui.plasmo_voice.off");
    }

    public void invertToggle() {
        this.toggled.invert();
        this.setMessage(getText());
        if (action != null) {
            action.onToggle(this.toggled.get());
        }
    }

    protected int getYImage(boolean hovered) {
        return 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.invertToggle();
    }

    protected void renderBg(PoseStack matrices, Minecraft client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int i = (this.isHoveredOrFocused() && this.active ? 2 : 1) * 20;
        if (this.toggled.get()) {
            blit(matrices, this.getX() + (int)((double)(this.width - 9)), this.getY(), 0, 46 + i, 4, 20);
            blit(matrices, this.getX() + (int)((double)(this.width - 9)) + 4, this.getY(), 196, 46 + i, 4, 20);
        } else {
            blit(matrices, this.getX(), this.getY(), 0, 46 + i, 4, 20);
            blit(matrices, this.getX() + 4, this.getY(), 196, 46 + i, 4, 20);
        }
    }

    public interface PressAction {
        void onToggle(boolean toggled);
    }
}
