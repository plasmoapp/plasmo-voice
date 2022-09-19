package su.plo.lib.client.gui.components;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.MathLib;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.lib.client.gui.widget.GuiWidget;
import su.plo.lib.client.gui.widget.GuiWidgetListener;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractScrollbar<P extends GuiScreen> extends AbstractScreenListener implements GuiWidget {

    protected final List<Entry> entries = Lists.newArrayList();
    protected final List<EntryPosition> entryPositions = Lists.newArrayList();

    protected final MinecraftClientLib minecraft;

    protected final P parent;
    @Getter
    protected final int containerWidth;
    @Getter
    protected final int width;
    @Getter
    protected final int height;

    protected int y0;
    protected int y1;
    protected int scrollHeight;
    protected double scrollTop;
    protected boolean scrolling;
    protected @Nullable Entry hoveredEntry;

    public AbstractScrollbar(MinecraftClientLib minecraft,
                             P parent,
                             int containerWidth,
                             int y0, int y1) {
        this.minecraft = minecraft;
        this.parent = parent;

        this.containerWidth = containerWidth;
        this.width = parent.getWidth();
        this.height = parent.getHeight();

        this.y0 = y0;
        this.y1 = y1;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            this.hoveredEntry = getEntryAtPosition(mouseX, mouseY).orElse(null);
        }

        int trackX0 = getScrollbarPosition();
        int trackX1 = trackX0 + 6;

        // render list
        renderList(render, getContainerX0(), y0, mouseX, mouseY, delta);

        int maxScroll = getMaxScroll();
        if (maxScroll > 0) {
            render.disableTexture();
            render.setShaderColor(1F, 1F, 1F, 1F);

            int trackBottom = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) scrollHeight);
            trackBottom = MathLib.clamp(trackBottom, 32, y1 - y0 - 8);
            int trackTop = (int) scrollTop * (y1 - y0 - trackBottom) / maxScroll + y0;
            if (trackTop < y0) {
                trackTop = y0;
            }

            render.fill(
                    trackX0, y0, trackX1, y1, -0x1000000
            );
            render.fill(
                    trackX0, trackTop, trackX1, trackTop + trackBottom, -0x7f7f80
            );
            render.fill(
                    trackX0, trackTop, trackX1 - 1, trackTop + trackBottom - 1, -0x3f3f40
            );
        }

        render.enableTexture();
        render.disableBlend();
    }

    // GuiScreenListener impl
    @Override
    public List<? extends GuiWidgetListener> widgets() {
        return entries;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        setScrollTop(scrollTop - delta * ((float) scrollHeight / entries.size())); // todo: use avg item height?
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 &&
                mouseX >= this.getScrollbarPosition() &&
                mouseX < (this.getScrollbarPosition() + 6);

        Optional<Entry> entry = getEntryAtPosition(mouseX, mouseY);

        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (entry.isPresent()) {
            if (entry.get().mouseClicked(mouseX, mouseY, button)) {
                setFocused(entry.get());
                setDragging(true);
                return true;
            }
        }

        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (getFocused() != null) {
            getFocused().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        } else if (button == 0 && scrolling) {
            if (mouseY < y0) {
                setScrollTop(0D);
            } else if (mouseY > y1) {
                setScrollTop(getMaxScroll());
            } else {
                double maxScroll = Math.max(1, getMaxScroll());
                int diff = y1 - y0;
                int scrollStep = MathLib.clamp((int)((float)(diff * diff) / (float)scrollHeight), 32, diff);
                double multiplier = Math.max(1, maxScroll / (double)(diff - scrollStep));

                setScrollTop(scrollTop + deltaY * multiplier);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= y0 &&
                mouseY <= y1 &&
                mouseX >= 0 &&
                mouseX <= width;
    }

    // Class methods

    // entries
    public void addEntry(@NotNull Entry entry) {
        entries.add(entry);

        EntryPosition lastPosition = getLastEntryPosition();
        entryPositions.add(new EntryPosition(lastPosition.bottom, lastPosition.bottom + entry.getHeight()));

        this.scrollHeight += entry.getHeight();
    }

    public void clearEntries() {
        entries.clear();
        entryPositions.clear();
        this.scrollHeight = 0;
    }

    public void onEntryHeightChange() {
        this.scrollHeight = entries.stream()
                .map(Entry::getHeight)
                .reduce(0, Integer::sum);
    }

    // ???
    public void setScrollTop(double scrollTop) {
        this.scrollTop = MathLib.clamp(scrollTop, 0D, getMaxScroll());
    }

    protected void renderList(@NotNull GuiRender render, int x, int y, int mouseX, int mouseY, float delta) {
        render.enableScissor(0, y0 - 4, width, y1 + 4);
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            EntryPosition position = entryPositions.get(index);

            int entryTop = y - (int) scrollTop + position.top;
            int entryBottom = y - (int) scrollTop + position.bottom;

            if (entryTop > y1 || entryBottom < y0) continue;

            entry.render(render, index, x, entryTop, containerWidth, mouseX, mouseY, Objects.equals(hoveredEntry, entry), delta);
        }
        render.disableScissor();
    }

    protected int getContainerX0() {
        return (width - containerWidth) / 2;
    }

    protected int getContainerX1() {
        return getContainerX0() + containerWidth;
    }

    protected int getScrollbarPosition() {
        return getContainerX1() + 13;
    }

    protected Optional<Entry> getEntryAtPosition(double mouseX, double mouseY) {
        if (mouseX < getContainerX0() || mouseX > getContainerX1()) return Optional.empty();

        double relativeY = mouseY - y0 + scrollTop;
        if (relativeY < 0 || relativeY > scrollHeight) return Optional.empty();

        for (int index = 0; index < entries.size(); index++) {
            EntryPosition position = entryPositions.get(index);
            if (relativeY > position.top && relativeY <= position.bottom) {
                return Optional.of(entries.get(index));
            }
        }

        return Optional.empty();
    }

    private int getMaxScroll() {
        return Math.max(0, scrollHeight - (y1 - y0));
    }

    private @NotNull EntryPosition getLastEntryPosition() {
        if (entryPositions.size() == 0) return EntryPosition.EMPTY;
        return entryPositions.get(entryPositions.size() - 1);
    }

    public abstract void init();

    @AllArgsConstructor
    @ToString
    static class EntryPosition {

        static EntryPosition EMPTY = new EntryPosition(0, 0);

        private int top;
        private int bottom;
    }

    public abstract class Entry extends AbstractScreenListener {

        @Getter
        protected int height;

        public Entry(int height) {
            this.height = height;
        }

        public void setHeight(int height) {
            this.height = height;
            AbstractScrollbar.this.onEntryHeightChange();
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return Collections.emptyList();
        }

        public abstract void render(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta);
    }
}
