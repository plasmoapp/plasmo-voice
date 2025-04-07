package su.plo.voice.client.gui.settings.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

import java.util.List;
import java.util.function.Consumer;

public final class DropDownWidget extends GuiAbstractWidget {

    private final DropDownWidgetList list;

    private boolean open;

    public DropDownWidget(
            @NotNull VoiceSettingsScreen parent,
            int x,
            int y,
            int width,
            int height,
            @NotNull McTextComponent message,
            @NotNull List<McTextComponent> elements,
            boolean tooltip,
            @NotNull Consumer<Integer> onSelect
    ) {
        super(x, y, width, height, message);

        this.list = new DropDownWidgetList(
                this,
                elements,
                parent,
                width,
                tooltip,
                index -> {
                    this.text = elements.get(index);
                    switchOpen();
                    playDownSound();
                    onSelect.accept(index);
                }
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (open && list.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            switchOpen();
            return true;
        }

        if (!open) return false;

        switchOpen();
        playDownSound();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return open && list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) ||
                super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void renderButton(@NotNull PoseStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack, mouseX, mouseY);
        renderArrow(stack);
        renderText(stack);

        if (!open) return;

        list.render(stack, mouseX, mouseY, delta);
    }

    @Override
    protected void renderBackground(@NotNull PoseStack stack, int mouseX, int mouseY) {
        RenderUtil.fill(stack, x, y, x + width, y + height, -6250336);
        RenderUtil.fill(stack, x + 1, y + 1, x + width - 1, y + height - 1, -16777216);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return open && list.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return (open && list.isMouseOver(mouseX, mouseY)) || super.isMouseOver(mouseX, mouseY);
    }

    private void switchOpen() {
        if (open) {
            this.open = false;
            list.setScrollTop(0.0D);
        } else {
            this.open = true;
        }
    }

    private void renderText(@NotNull PoseStack stack) {
        RenderUtil.drawOrderedString(
                stack,
                getText(),
                active ? (width - 23) : (width - 5),
                x + 5,
                y + (height / 2) - (RenderUtil.getFontHeight() / 2),
                active ? 0xE0E0E0 : 0x707070
        );
    }

    private void renderArrow(@NotNull PoseStack stack) {
        if (!active) return;

        if (open) {
            for (int i = 0; i < 5; i++) {
                RenderUtil.fill(
                        stack,
                        x + width - (9 + i),
                        y + ((height - 5) / 2) + i,
                        x + width - (8 - i),
                        (y + (height - 5) / 2) + 2 + i,
                        -0x5F5F60
                );
            }
        } else {
            for (int i = 0; i < 5; i++) {
                RenderUtil.fill(
                        stack,
                        x + width - (13 - i),
                        y + ((height - 5) / 2) + (i > 0 ? (1 + i) : 0),
                        x + width - (4 + i),
                        (y + (height - 5) / 2) + 2 + i,
                        -0x5F5F60
                );
            }
        }
    }
}
