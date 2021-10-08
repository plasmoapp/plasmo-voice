package su.plo.voice.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.config.entries.BooleanConfigEntry;

public class ToggleButton extends AbstractWidget {
    private final ToggleButton.PressAction action;
    private final BooleanConfigEntry toggled;

    public ToggleButton(int x, int y, int width, int height, BooleanConfigEntry toggled, ToggleButton.PressAction action) {
        super(x, y, width, height, toggled.get() ? new TranslatableComponent("gui.plasmo_voice.on") : new TranslatableComponent("gui.plasmo_voice.off"));
        this.toggled = toggled;
        this.action = action;
    }

    public void updateValue() {
        this.setMessage(getText());
    }

    private Component getText() {
        return toggled.get() ? new TranslatableComponent("gui.plasmo_voice.on") : new TranslatableComponent("gui.plasmo_voice.off");
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
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.invertToggle();
    }

    protected void renderBg(PoseStack matrices, Minecraft client, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        int i = (this.isHovered() && this.active ? 2 : 1) * 20;
        if (this.toggled.get()) {
            blit(matrices, this.x + (int)((double)(this.width - 9)), this.y, 0, 46 + i, 4, 20);
            blit(matrices, this.x + (int)((double)(this.width - 9)) + 4, this.y, 196, 46 + i, 4, 20);
        } else {
            blit(matrices, this.x, this.y, 0, 46 + i, 4, 20);
            blit(matrices, this.x + 4, this.y, 196, 46 + i, 4, 20);
        }
    }

    public interface PressAction {
        void onToggle(boolean toggled);
    }
}
