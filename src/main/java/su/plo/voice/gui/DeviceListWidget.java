package su.plo.voice.gui;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

import java.util.ArrayList;
import java.util.List;

public class DeviceListWidget extends ElementListWidget<DeviceListWidget.DeviceEntry> {
    private final Text title;

    public DeviceListWidget(MinecraftClient client, int width, int height, int top, Text title) {
        super(client, width, height, top, top + height - 12, 24);
        this.title = title;
        this.method_31322(false);
        this.method_31323(false);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.client.getWindow().getScaleFactor();
        RenderSystem.enableScissor((int)((double)this.getRowLeft() * d),
                (int)((double)(this.top) * d),
                (int)((double)(this.getScrollbarPositionX() + 6) * d),
                (int)((double)(160) * d));
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
        int x = this.left - 2;
        int y = this.top - 24;

        Text text = (new LiteralText("")).append(this.title);
        this.client.textRenderer.draw(matrices, text, (float)(x + this.width / 2 - this.client.textRenderer.getWidth(text) / 2), (float) y, 16777215);
    }

    public int getRowWidth() {
        return this.width;
    }

    protected int getScrollbarPositionX() {
        return this.right - 14;
    }

    public static class DeviceEntry extends ElementListWidget.Entry<DeviceListWidget.DeviceEntry> {
        private final String device;
        private final MinecraftClient client;

        private final List<Element> childrens = new ArrayList<>();
        private final OrderedText orderedText;
        private final SelectAction onSelect;

        public DeviceEntry(MinecraftClient client, String device, SelectAction onSelect) {
            this.client = client;
            this.device = device;
            this.onSelect = onSelect;
            this.orderedText = method_31229(client, new LiteralText(this.device));
        }

        private static OrderedText method_31229(MinecraftClient minecraftClient, Text text) {
            int i = minecraftClient.textRenderer.getWidth(text);
            if (i > 216) {
                StringVisitable stringVisitable = StringVisitable.concat(minecraftClient.textRenderer.trimToWidth(text, 216 - minecraftClient.textRenderer.getWidth("...")), StringVisitable.plain("..."));
                return Language.getInstance().reorder(stringVisitable);
            } else {
                return text.asOrderedText();
            }
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            entryWidth -= 8;
            entryHeight += 4;
            if(hovered) {
                DrawableHelper.fill(matrices, x, y, x + entryWidth, y + entryHeight, -1601138544);
            }

            this.client.textRenderer.drawWithShadow(matrices, this.orderedText,
                    (float) (x + 8), (float) (y + 1 + (entryHeight - this.client.textRenderer.fontHeight) / 2), 16777215);
        }

        @Override
        public List<? extends Element> children() {
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
