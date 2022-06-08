package su.plo.voice.client.gui.tabs;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import su.plo.voice.client.config.entries.ConfigEntry;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widgets.DropDownWidget;
import su.plo.voice.client.gui.widgets.KeyBindWidget;
import su.plo.voice.client.gui.widgets.NumberTextFieldWidget;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TabWidget extends ContainerObjectSelectionList<TabWidget.Entry> {
    private final VoiceSettingsScreen parent;
    private Entry hoveredEntry;
    private boolean scrolling;

    public TabWidget(Minecraft client, VoiceSettingsScreen parent) {
//        super(minecraftClient, width, height, top, bottom, itemHeight);
        super(client, parent.width, parent.height, parent.getHeaderHeight() + 4, parent.height - 4, 24);
        this.parent = parent;
        this.setRenderBackground(false);
        this.setRenderHeader(false, 0);
        this.setRenderTopAndBottom(false);
    }

    public void onClose() {
        this.setScrollAmount(0);
    }

    public void setTooltip(List<Component> tooltip) {
        parent.setTooltip(tooltip);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 40;
    }

    @Override
    public int getRowWidth() {
        return 302;
    }

    @Override
    protected int getMaxPosition() {
        int height = 0;

        for (Entry entry : children()) {
            height += entry.getHeight();
        }

        return height + this.headerHeight;
    }

    @Override
    protected void centerScrollOn(Entry entry) {
        int height = 0;
        for (Entry e : children()) {
            if (e == entry) {
                break;
            }

            height += e.getHeight();
        }

        this.setScrollAmount((double)(height + entry.getHeight() / 2 - (this.y1 - this.y0) / 2));
    }

    private void scroll(int amount) {
        this.setScrollAmount(this.getScrollAmount() + (double)amount);
    }

    @Override
    protected void ensureVisible(Entry entry) {
        int i = this.getRowTop(this.children().indexOf(entry));
        // todo wtf how it works pepega
        int j = i - this.y0 - 4 - this.itemHeight;
        if (j < 0) {
            this.scroll(j);
        }

        int k = this.y1 - i - this.itemHeight - this.itemHeight;
        if (k < 0) {
            this.scroll(-k);
        }
    }

    @Override
    protected int getRowTop(int index) {
        int height = 0;
        for (int i = 0; i < index; i++) {
            height += children().get(i).getHeight();
        }

        return this.y0 + 4 - (int)this.getScrollAmount() + height + this.headerHeight;
    }

    private int getRowBottom(int index) {
        return this.getRowTop(index) + children().get(index).getHeight();
    }

    private Entry getDynamicEntryAtPosition(double x, double y) {
        int i = this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2;
        int k = j - i;
        int l = j + i;
        int m = Mth.floor(y - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
        if (x < (double)this.getScrollbarPosition() && x >= (double)k && x <= (double)l) {
            int top = 0;
            for (Entry entry : children()) {
                if (m >= top && m <= top + entry.getHeight()) {
                    return entry;
                }

                top += entry.getHeight();
            }
        }

        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        this.setScrollAmount(this.getScrollAmount() - amount * (double)this.itemHeight / 2.0D);
        return true;
    }

    // todo fix drag out the slider not playing a sound
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Entry entry = this.getDynamicEntryAtPosition(mouseX, mouseY);

        for (Entry e : children()) {
            if (entry != e && e instanceof OptionEntry) {
                if (e.children().get(0) instanceof NumberTextFieldWidget) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(e);
                        this.setDragging(true);
                        return true;
                    }
                } else if (e.children().get(0) instanceof DropDownWidget) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                } else if (e.children().get(0) instanceof KeyBindWidget) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        }

        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (button == 0) {
                this.clickedHeader((int)(mouseX - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.y0) + (int)this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Entry entry = this.getDynamicEntryAtPosition(mouseX, mouseY);

        for (Entry e : children()) {
            if (entry != e && e instanceof OptionEntry) {
                if (e.children().get(0) instanceof KeyBindWidget) {
                    e.mouseReleased(mouseX, mouseY, button);
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
        super.updateScrollingState(mouseX, mouseY, button);
    }

    @Override
    public int getRowLeft() {
        return super.getRowLeft();
    }

    @Override
    protected void renderList(PoseStack matrices, int x, int y, int mouseX, int mouseY, float delta) {
        int i = this.getItemCount();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        int height = 0;
        for(int j = 0; j < i; ++j) {
            int k = this.getRowTop(j);
            int l = this.getRowBottom(j);
            if (l >= this.y0 && k <= this.y1) {
                Entry entry = this.getEntry(j);
                int m = y + height + this.headerHeight;
                int n = entry.getHeight() - 4;
                int o = this.getRowWidth();
                int r;
                if (this.isSelectedItem(j)) { // && this.renderSelection (useless)
                    r = this.x0 + this.width / 2 - o / 2;
                    int q = this.x0 + this.width / 2 + o / 2;
                    RenderSystem.disableTexture();
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    float f = this.isFocused() ? 1.0F : 0.5F;
                    RenderSystem.setShaderColor(f, f, f, 1.0F);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferBuilder.vertex((double)r, (double)(m + n + 2), 0.0D).endVertex();
                    bufferBuilder.vertex((double)q, (double)(m + n + 2), 0.0D).endVertex();
                    bufferBuilder.vertex((double)q, (double)(m - 2), 0.0D).endVertex();
                    bufferBuilder.vertex((double)r, (double)(m - 2), 0.0D).endVertex();
                    tessellator.end();
                    RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                    bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                    bufferBuilder.vertex((double)(r + 1), (double)(m + n + 1), 0.0D).endVertex();
                    bufferBuilder.vertex((double)(q - 1), (double)(m + n + 1), 0.0D).endVertex();
                    bufferBuilder.vertex((double)(q - 1), (double)(m - 1), 0.0D).endVertex();
                    bufferBuilder.vertex((double)(r + 1), (double)(m - 1), 0.0D).endVertex();
                    tessellator.end();
                    RenderSystem.enableTexture();
                }

                r = this.getRowLeft();
                entry.render(matrices, j, k, r, o, n, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);

                height += entry.getHeight();
            }
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getDynamicEntryAtPosition(mouseX, mouseY) : null;
        super.render(matrices, mouseX, mouseY, delta);
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        @Getter
        private int height;

        public Entry(int height) {
            this.height = height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public class OptionEntry extends Entry {
        private final Component text;
        private final List<Component> tooltip;
        private final AbstractWidget element;
        private final Button resetButton;
        private final ConfigEntry entry;

        public OptionEntry(Component text, AbstractWidget element, ConfigEntry entry, ResetAction action) {
            this(text, element, entry, null, action);
        }

        public OptionEntry(Component text, AbstractWidget element, ConfigEntry entry, List<Component> tooltip, ResetAction action) {
            super(24);
            this.text = text;
            this.element = element;
            this.entry = entry;
            this.tooltip = tooltip;

            this.resetButton = new Button(0, 0, 46, 20, Component.translatable("controls.reset"), button -> {
                if (entry != null) {
                    entry.reset();
                    if (action != null) {
                        action.onReset(button, element);
                    }
                }
            });
        }

        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int elementY = y + entryHeight - 4;
            Objects.requireNonNull(TabWidget.this.minecraft.font);
            TabWidget.this.minecraft.font.drawShadow(matrices, this.text, x, (float)(elementY - 9 - 1), 16777215);

            element.x = x + entryWidth - 147;
            element.y = y;
            element.render(matrices, mouseX, mouseY, tickDelta);

            resetButton.x = x + entryWidth - 46;
            resetButton.y = y;
            resetButton.active = entry != null && !entry.isDefault();
            resetButton.render(matrices, mouseX, mouseY, tickDelta);

            if (hovered && mouseX < (element.x - 4)) {
                TabWidget.this.setTooltip(tooltip);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.element, this.resetButton);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (GuiEventListener entry : children()) {
                if (entry instanceof KeyBindWidget && entry.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.element, this.resetButton);
        }

        public interface ResetAction {
            void onReset(Button resetButton, AbstractWidget element);
        }
    }

    public class CategoryEntry extends Entry {
        final Component text;
        private final int textWidth;
        private int color = 16777215;

        public CategoryEntry(Component text) {
            super(24);
            this.text = text;
            this.textWidth = TabWidget.this.minecraft.font.width(this.text);
        }

        public CategoryEntry(Component text, int height) {
            super(height);
            this.text = text;
            this.textWidth = TabWidget.this.minecraft.font.width(this.text);
        }

        @Override
        public void render(PoseStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            float elementX = (float)(TabWidget.this.minecraft.screen.width / 2 - this.textWidth / 2);
            int elementY = y + entryHeight - 4;
            Objects.requireNonNull(TabWidget.this.minecraft.font);

            TabWidget.this.minecraft.font.drawShadow(matrices, this.text, elementX, (float)(elementY - 9 - 1), color);
        }

        @Override
        public boolean changeFocus(boolean lookForwards) {
            return false;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(new NarratableEntry() {
                public NarrationPriority narrationPriority() {
                    return NarrationPriority.HOVERED;
                }

                public void updateNarration(NarrationElementOutput builder) {
                    builder.add(NarratedElementType.TITLE, CategoryEntry.this.text);
                }
            });
        }
    }

}
