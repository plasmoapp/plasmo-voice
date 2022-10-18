package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.widget.GuiAbstractWidget;
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

    public DropDownWidget(@NotNull MinecraftClientLib minecraft,
                          @NotNull VoiceSettingsScreen parent,
                          int x,
                          int y,
                          int width,
                          int height,
                          @NotNull MinecraftTextComponent message,
                          @NotNull List<MinecraftTextComponent> elements,
                          boolean tooltip,
                          @NotNull Consumer<Integer> onSelect) {
        super(minecraft, x, y, width, height, message);

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
    public void renderButton(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        renderBackground(render, mouseX, mouseY);
        renderArrow(render);
        renderText(render);

        if (!open) return;
        renderElements(render, mouseX, mouseY);
    }

    @Override
    protected void renderBackground(@NotNull GuiRender render, int mouseX, int mouseY) {
        render.fill(x, y, x + width, y + height, -6250336);
        render.fill(x + 1, y + 1, x + width - 1, y + height - 1, -16777216);
    }

    private void renderElements(@NotNull GuiRender render, int mouseX, int mouseY) {
        int elementY;

        boolean renderToTop = renderToTop();

        if (renderToTop) {
            elementY = y - (elements.size() * (ELEMENT_HEIGHT + 1));
        } else {
            elementY = y + height;
        }

        for (MinecraftTextComponent element : elements) {
            render.enableDepthTest();
            render.getMatrix().push();
            render.getMatrix().translate(0D, 0D, 10D);

            if (renderToTop) {
                render.fill(x, elementY - 1, x + width, elementY + ELEMENT_HEIGHT, -0xB9B9BA);
            } else {
                render.fill(x, elementY, x + width, elementY + ELEMENT_HEIGHT + 1, -0xB9B9BA);
            }
            render.fill(x + 1, elementY, x + width - 1, elementY + ELEMENT_HEIGHT, -0x1000000);

            if ((mouseX >= x && mouseX <= x + width) &&
                    (mouseY >= elementY && mouseY <= elementY + ELEMENT_HEIGHT)) {
                if (tooltip && minecraft.getFont().width(element) > (width - 10)) {
                    parent.setTooltip(ImmutableList.of(element));
                }
                render.fill(x + 1, elementY, x + width - 1, elementY + ELEMENT_HEIGHT, -0xCDCDCE);
            }

            render.drawOrderedString(
                    element,
                    width - 10,
                    x + 5,
                    elementY + ELEMENT_HEIGHT / 2 - minecraft.getFont().getLineHeight() / 2,
                    0xE0E0E0
            );

            render.getMatrix().pop();
            render.disableDepthTest();

            elementY += ELEMENT_HEIGHT + 1;
        }
    }

    private void renderText(@NotNull GuiRender render) {
        render.drawOrderedString(
                getText(),
                active ? (width - 21) : (width - 5),
                x + 5,
                y + (height / 2) - (minecraft.getFont().getLineHeight() / 2),
                active ? 0xE0E0E0 : 0x707070
        );
    }

    private void renderArrow(@NotNull GuiRender render) {
        if (!active) return;

        if (open) {
            for (int i = 0; i < 5; i++) {
                render.fill(
                        x + width - (9 + i),
                        y + ((height - 5) / 2) + i,
                        x + width - (8 - i),
                        (y + (height - 5) / 2) + 2 + i,
                        -0x5F5F60
                );
            }
        } else {
            for (int i = 0; i < 5; i++) {
                render.fill(
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
        return (y + height + 1 + (elements.size() * (ELEMENT_HEIGHT + 1)) > minecraft.getWindow().getGuiScaledHeight()) &&
                (parent.getNavigation().getHeight() + height + 1 + (elements.size() * (ELEMENT_HEIGHT + 1)) < minecraft.getWindow().getGuiScaledHeight());
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
