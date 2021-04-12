package su.plo.voice.gui;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.*;

import java.util.ArrayList;
import java.util.List;

public class DeviceListWidget extends AbstractOptionList<DeviceListWidget.DeviceEntry> {
    private final ITextComponent title;

    public DeviceListWidget(Minecraft client, int width, int height, int top, ITextComponent title) {
        super(client, width, height, top, top + height - 12, 24);
        this.title = title;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.minecraft.getWindow().getGuiScale();
        RenderSystem.enableScissor((int)((double)this.getRowLeft() * d),
                (int)((double)(this.getTop()) * d),
                (int)((double)(this.getScrollbarPosition() + 6) * d),
                (int)((double)(160) * d));
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
        int x = this.getLeft() - 2;
        int y = this.getTop() - 24;

        ITextComponent text = (new StringTextComponent("")).append(this.title);
        this.minecraft.font.draw(matrices, text, (float)(x + this.width / 2 - this.minecraft.font.width(text) / 2), (float) y, 16777215);
    }
    public int getRowWidth() {
        return this.width;
    }

    protected int getScrollbarPosition() {
        return this.getRight() - 14;
    }

    public static class DeviceEntry extends AbstractOptionList.Entry<DeviceEntry> {
        private final String device;
        private final Minecraft client;

        private final List<Widget> childrens = new ArrayList<>();
        private final IReorderingProcessor orderedText;
        private final SelectAction onSelect;

        public DeviceEntry(Minecraft client, String device, SelectAction onSelect) {
            this.client = client;
            this.device = device;
            this.onSelect = onSelect;
            this.orderedText = method_31229(client, new StringTextComponent(this.device));
        }

        private static IReorderingProcessor method_31229(Minecraft minecraftClient, ITextComponent text) {
            int i = minecraftClient.font.width(text);
            if (i > 216) {
                ITextProperties stringVisitable = ITextProperties.composite(minecraftClient.font.substrByWidth(text, 216 - minecraftClient.font.width("...")), ITextProperties.of("..."));
                return LanguageMap.getInstance().getVisualOrder(stringVisitable);
            } else {
                return text.getVisualOrderText();
            }
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            entryWidth -= 8;
            entryHeight += 4;
            if(hovered) {
                AbstractGui.fill(matrices, x, y, x + entryWidth, y + entryHeight, -1601138544);
            }

            this.client.font.drawShadow(matrices, this.orderedText,
                    (float) (x + 8), (float) (y + 1 + (entryHeight - this.client.font.lineHeight) / 2), 16777215);
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return this.childrens;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.onSelect.onSelect(this.device);
            return true;
        }

        public interface SelectAction {
            void onSelect(String device);
        }
    }
}
