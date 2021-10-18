package su.plo.voice.client.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import su.plo.voice.client.gui.VoiceSettingsScreen;

import java.util.List;
import java.util.function.Consumer;

public class DropDownWidget extends AbstractWidget implements Widget {
    private boolean open;
    private final Minecraft client = Minecraft.getInstance();
    private final Font textRenderer;
    private final List<Component> elements;
    private final int elementHeight = 16;
    private final Consumer<Integer> onSelect;
    private final VoiceSettingsScreen parent;
    private final boolean tooltip;

    public DropDownWidget(VoiceSettingsScreen parent, int x, int y, int width, int height, Component message, List<Component> elements, boolean tooltip, Consumer<Integer> onSelect) {
        super(x, y, width - 2, height - 1, message);
        this.tooltip = tooltip;
        this.parent = parent;
        this.textRenderer = client.font;
        this.elements = elements;
        this.onSelect = onSelect;
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
                this.playDownSound(client.getSoundManager());
            }
            return true;
        }

        return false;
    }

    private boolean elementClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {
            if (this.y + this.height + 1 + (elements.size() * (elementHeight + 1)) > client.getWindow().getGuiScaledHeight()) {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + 1 - (elements.size() * (elementHeight + 1)) && mouseY <= y)) {
                    int i = (int) Math.floor((mouseY - (y + 1 - (elements.size() * (elementHeight + 1)))) / (elementHeight + 1));
                    this.playDownSound(client.getSoundManager());
                    this.setMessage(elements.get(i));
                    if (this.onSelect != null) {
                        this.onSelect.accept(i);
                    }
                    return true;
                }
            } else {
                if ((mouseX >= x && mouseX <= x + width) &&
                        (mouseY >= y + height && mouseY <= y + height + 1 + (elements.size() * (elementHeight + 1)))) {
                    int i = (int) Math.floor((mouseY - (y + height + 1)) / (elementHeight + 1));
                    this.playDownSound(client.getSoundManager());
                    this.setMessage(elements.get(i));
                    if (this.onSelect != null) {
                        this.onSelect.accept(i);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private void renderArrow(PoseStack matrices) {
        if (open) {
            for (int i = 0; i < 5; i++) {
                fill(matrices, this.x + this.width - (9 + i), this.y + ((this.height - 5) / 2) + i,
                        this.x + this.width - (8 - i), (this.y + (this.height - 5) / 2) + 2 + i, -6250336);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                fill(matrices, this.x + this.width - (13 - i), this.y + ((this.height - 5) / 2) + (i > 0 ? (1 + i) : 0),
                        this.x + this.width - (4 + i), (this.y + (this.height - 5) / 2) + 2 + i, -6250336);
            }
        }
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        fill(matrices, this.x, this.y, this.x + this.width + 1, this.y + this.height + 1, -6250336);
        fill(matrices, this.x + 1, this.y + 1, this.x + this.width, this.y + this.height, -16777216);

        renderArrow(matrices);

        this.textRenderer.drawShadow(matrices, orderedText(client, getMessage(), this.width - 21),
                (float)this.x + 5, (float)this.y + 1 + (this.height - 8) / 2, 14737632);

        if (open) {
            if ((this.y + this.height + 1 + (elements.size() * (elementHeight + 1)) > client.getWindow().getGuiScaledHeight()) &&
                    (parent.getHeaderHeight() + this.height + 1 + (elements.size() * (elementHeight + 1)) < client.getWindow().getGuiScaledHeight())) {
                int elementY = this.y + 1 - (elements.size() * (elementHeight + 1));

                for (Component element : elements) {
                    matrices.pushPose();
                    RenderSystem.enableDepthTest();
                    matrices.translate(0.0D, 0.0D, 10.0D);

                    fill(matrices, this.x, elementY - 1, this.x + this.width + 1, elementY + elementHeight, -12171706);
                    fill(matrices, this.x + 1, elementY, this.x + this.width, elementY + elementHeight, -16777216);
                    if ((mouseX >= x && mouseX <= x + width) &&
                            (mouseY >= elementY && mouseY <= elementY + elementHeight)) {
                        if (tooltip && this.textRenderer.width(element) > (this.width - 10)) {
                            parent.setTooltip(ImmutableList.of(element));
                        }
                        fill(matrices, this.x + 1, elementY, this.x + this.width, elementY + elementHeight, -13487566);
                    }
                    this.textRenderer.drawShadow(matrices, orderedText(client, element, this.width - 10),
                            (float)this.x + 5, (float)elementY + 1 + (elementHeight - 8) / 2, 14737632);


                    matrices.popPose();

                    elementY += elementHeight + 1;
                }
            } else {
                int elementY = this.y + this.height + 1;

                for (Component element : elements) {
                    matrices.pushPose();
                    RenderSystem.enableDepthTest();
                    matrices.translate(0.0D, 0.0D, 10.0D);

                    fill(matrices, this.x, elementY, this.x + this.width + 1, elementY + elementHeight + 1, -12171706);
                    fill(matrices, this.x + 1, elementY, this.x + this.width, elementY + elementHeight, -16777216);
                    if ((mouseX >= x && mouseX <= x + width) &&
                            (mouseY >= elementY && mouseY <= elementY + elementHeight)) {
                        if (tooltip && this.textRenderer.width(element) > (this.width - 10)) {
                            parent.setTooltip(ImmutableList.of(element));
                        }
                        fill(matrices, this.x + 1, elementY, this.x + this.width, elementY + elementHeight, -13487566);
                    }
                    this.textRenderer.drawShadow(matrices, orderedText(client, element, this.width - 10),
                            (float)this.x + 5, (float)elementY + 1 + (elementHeight - 8) / 2, 14737632);


                    matrices.popPose();

                    elementY += elementHeight + 1;
                }
            }
        }
    }

    private FormattedCharSequence orderedText(Minecraft minecraftClient, Component text, int width) {
        int i = minecraftClient.font.width(text);
        if (i > width) {
            FormattedText stringVisitable = FormattedText.composite(minecraftClient.font.substrByWidth(text, width - minecraftClient.font.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(stringVisitable);
        } else {
            return text.getVisualOrderText();
        }
    }
}
