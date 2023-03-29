package su.plo.voice.client.gui.settings.widget;

import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UResolution;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

import java.util.List;
import java.util.function.Consumer;

public final class DropDownWidget extends GuiAbstractWidget {

    private static final int ELEMENT_HEIGHT = 16;

    private final VoiceSettingsScreen parent;
    private final List<MinecraftTextComponent> elements;
    private final boolean tooltip;
    private final Consumer<Integer> onSelect;

    private boolean open;

    public DropDownWidget(@NotNull VoiceSettingsScreen parent,
                          int x,
                          int y,
                          int width,
                          int height,
                          @NotNull MinecraftTextComponent message,
                          @NotNull List<MinecraftTextComponent> elements,
                          boolean tooltip,
                          @NotNull Consumer<Integer> onSelect) {
        super(x, y, width, height, message);

        this.parent = parent;
        this.elements = elements;
        this.onSelect = onSelect;
        this.tooltip = tooltip;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            open = !open;
            return true;
        }

        if (!open) return false;

        this.open = false;
        if (!elementClicked(mouseX, mouseY, button)) {
            playDownSound();
        }
        return true;
    }

    @Override
    public void renderButton(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        renderBackground(stack, mouseX, mouseY);
        renderArrow(stack);
        renderText(stack);

        if (!open) return;
        renderElements(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBackground(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        UGraphics.enableBlend();
        RenderUtil.defaultBlendFunc();
        UGraphics.enableDepth();

        RenderUtil.fill(stack, x, y, x + width, y + height, -6250336);
        RenderUtil.fill(stack, x + 1, y + 1, x + width - 1, y + height - 1, -16777216);
    }

    private void renderElements(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int elementY;

        boolean renderToTop = renderToTop();

        if (renderToTop) {
            elementY = y - (elements.size() * (ELEMENT_HEIGHT + 1));
        } else {
            elementY = y + height;
        }

        for (MinecraftTextComponent element : elements) {
            UGraphics.enableDepth();

            stack.push();
            stack.translate(0D, 0D, 10D);

            if (renderToTop) {
                RenderUtil.fill(stack, x, elementY - 1, x + width, elementY + ELEMENT_HEIGHT, -0xB9B9BA);
            } else {
                RenderUtil.fill(stack, x, elementY, x + width, elementY + ELEMENT_HEIGHT + 1, -0xB9B9BA);
            }
            RenderUtil.fill(stack, x + 1, elementY, x + width - 1, elementY + ELEMENT_HEIGHT, -0x1000000);

            if ((mouseX >= x && mouseX <= x + width) &&
                    (mouseY >= elementY && mouseY <= elementY + ELEMENT_HEIGHT)) {
                if (tooltip && RenderUtil.getTextWidth(element) > (width - 10)) {
                    parent.setTooltip(element);
                }
                RenderUtil.fill(stack, x + 1, elementY, x + width - 1, elementY + ELEMENT_HEIGHT, -0xCDCDCE);
            }

            RenderUtil.drawOrderedString(
                    stack,
                    element,
                    width - 10,
                    x + 5,
                    elementY + ELEMENT_HEIGHT / 2 - UGraphics.getFontHeight() / 2,
                    0xE0E0E0
            );

            stack.pop();
            UGraphics.disableDepth();

            elementY += ELEMENT_HEIGHT + 1;
        }
    }

    private void renderText(@NotNull UMatrixStack stack) {
        RenderUtil.drawOrderedString(
                stack,
                getText(),
                active ? (width - 23) : (width - 5),
                x + 5,
                y + (height / 2) - (UGraphics.getFontHeight() / 2),
                active ? 0xE0E0E0 : 0x707070
        );
    }

    private void renderArrow(@NotNull UMatrixStack stack) {
        if (!active) return;

        UGraphics.enableBlend();
        RenderUtil.defaultBlendFunc();
        UGraphics.enableDepth();

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

    private boolean renderToTop() {
        return (y + height + 1 + (elements.size() * (ELEMENT_HEIGHT + 1)) > UResolution.getScaledHeight()) &&
                (parent.getNavigation().getHeight() + height + 1 + (elements.size() * (ELEMENT_HEIGHT + 1)) < UResolution.getScaledHeight());
    }

    private boolean elementClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            if (renderToTop()) {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + 1 - (elements.size() * (ELEMENT_HEIGHT + 1)) && mouseY <= y + 1)) {
                    int i = (int) Math.floor((mouseY - (y + 1 - (elements.size() * (ELEMENT_HEIGHT + 1)))) / (ELEMENT_HEIGHT + 1));
                    playDownSound();
                    this.text = elements.get(i);
                    if (onSelect != null) {
                        onSelect.accept(i);
                    }
                    return true;
                }
            } else {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + 1 + height && mouseY <= y + 1 + height + (elements.size() * (ELEMENT_HEIGHT + 1)))) {
                    int i = (int) Math.floor((mouseY - (y + height + 1)) / (ELEMENT_HEIGHT + 1));
                    playDownSound();
                    this.text = elements.get(i);
                    if (onSelect != null) {
                        onSelect.accept(i);
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
