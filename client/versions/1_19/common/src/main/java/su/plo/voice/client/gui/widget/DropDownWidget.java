package su.plo.voice.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.VoiceSettingsScreen;

import java.util.List;
import java.util.function.Consumer;

public final class DropDownWidget extends AbstractWidget implements Widget, NarratableEntry {

    private static final Minecraft client = Minecraft.getInstance();

    private boolean open;
    private final Font textRenderer;
    private final List<Component> elements;
    private final int elementHeight = 16;
    private final Consumer<Integer> onSelect;
    private final VoiceSettingsScreen parent;
    private final boolean tooltip;

    public DropDownWidget(VoiceSettingsScreen parent,
                          int x,
                          int y,
                          int width,
                          int height,
                          Component message,
                          List<Component> elements,
                          boolean tooltip,
                          Consumer<Integer> onSelect) {
        super(x, y, width - 2, height - 1, message);
        this.tooltip = tooltip;
        this.parent = parent;
        this.textRenderer = client.font;
        this.elements = elements;
        this.onSelect = onSelect;
        if (elements.size() == 0) {
            this.active = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            open = !open;
            return true;
        }

        if (open) {
            open = false;
            if (!elementClicked(mouseX, mouseY, button)) {
                playDownSound(client.getSoundManager());
            }
            return true;
        }

        return false;
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        fill(poseStack, x, y, x + width + 1, y + height + 1, -6250336);
        fill(poseStack, x + 1, y + 1, x + width, y + height, -16777216);

        renderArrow(poseStack);

        textRenderer.drawShadow(
                poseStack,
                GuiUtil.getOrderedText(textRenderer, getMessage(), active ? (width - 21) : (width - 5)),
                (float) x + 5,
                (float) y + 1 + (float) (height - 8) / 2,
                active ? 0xE0E0E0 : 0x707070
        );

        if (open) {
            if ((y + height + 1 + (elements.size() * (elementHeight + 1)) > client.getWindow().getGuiScaledHeight()) &&
                    (parent.getNavigation().getHeight() + height + 1 + (elements.size() * (elementHeight + 1)) < client.getWindow().getGuiScaledHeight())) {
                int elementY = y + 1 - (elements.size() * (elementHeight + 1));

                for (Component element : elements) {
                    poseStack.pushPose();
                    RenderSystem.enableDepthTest();
                    poseStack.translate(0.0D, 0.0D, 10.0D);

                    fill(poseStack, x, elementY - 1, x + width + 1, elementY + elementHeight, -0xB9B9BA);
                    fill(poseStack, x + 1, elementY, x + width, elementY + elementHeight, -0x1000000);

                    if ((mouseX >= x && mouseX <= x + width) &&
                            (mouseY >= elementY && mouseY <= elementY + elementHeight)) {
                        if (tooltip && textRenderer.width(element) > (width - 10)) {
                            parent.setTooltip(ImmutableList.of(element));
                        }
                        fill(poseStack, x + 1, elementY, x + width, elementY + elementHeight, -0xCDCDCE);
                    }
                    textRenderer.drawShadow(
                            poseStack,
                            GuiUtil.getOrderedText(client.font, element, width - 10),
                            (float) x + 5, (float) elementY + 1 + (float) (elementHeight - 8) / 2,
                            0xE0E0E0
                    );


                    poseStack.popPose();

                    elementY += elementHeight + 1;
                }
            } else {
                int elementY = y + height + 1;

                for (Component element : elements) {
                    poseStack.pushPose();
                    RenderSystem.enableDepthTest();
                    poseStack.translate(0.0D, 0.0D, 10.0D);

                    fill(poseStack, x, elementY, x + width + 1, elementY + elementHeight + 1, -0xB9B9BA);
                    fill(poseStack, x + 1, elementY, x + width, elementY + elementHeight, -0x1000000);
                    if ((mouseX >= x && mouseX <= x + width) &&
                            (mouseY >= elementY && mouseY <= elementY + elementHeight)) {
                        if (tooltip && textRenderer.width(element) > (width - 10)) {
                            parent.setTooltip(ImmutableList.of(element));
                        }
                        fill(poseStack, x + 1, elementY, x + width, elementY + elementHeight, -0xCDCDCE);
                    }
                    textRenderer.drawShadow(
                            poseStack,
                            GuiUtil.getOrderedText(textRenderer, element, width - 10),
                            (float) x + 5, (float) elementY + 1 + (float) (elementHeight - 8) / 2,
                            0xE0E0E0
                    );


                    poseStack.popPose();

                    elementY += elementHeight + 1;
                }
            }
        }
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }

    private boolean elementClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            if (y + height + 1 + (elements.size() * (elementHeight + 1)) > client.getWindow().getGuiScaledHeight()) {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + 1 - (elements.size() * (elementHeight + 1)) && mouseY <= y + 1)) {
                    int i = (int) Math.floor((mouseY - (y + 1 - (elements.size() * (elementHeight + 1)))) / (elementHeight + 1));
                    playDownSound(client.getSoundManager());
                    setMessage(elements.get(i));
                    if (onSelect != null) {
                        onSelect.accept(i);
                    }
                    return true;
                }
            } else {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + 1 + height && mouseY <= y + 1 + height + (elements.size() * (elementHeight + 1)))) {
                    int i = (int) Math.floor((mouseY - (y + height + 1)) / (elementHeight + 1));
                    playDownSound(client.getSoundManager());
                    setMessage(elements.get(i));
                    if (onSelect != null) {
                        onSelect.accept(i);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void renderArrow(PoseStack poseStack) {
        if (!active) return;

        if (open) {
            for (int i = 0; i < 5; i++) {
                fill(poseStack,
                        x + width - (9 + i),
                        y + ((height - 5) / 2) + i,
                        x + width - (8 - i),
                        (y + (height - 5) / 2) + 2 + i,
                        -0x5F5F60
                );
            }
        } else {
            for (int i = 0; i < 5; i++) {
                fill(poseStack,
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
