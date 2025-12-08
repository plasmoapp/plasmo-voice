package su.plo.lib.mod.client.gui.components;

import com.google.common.collect.Lists;
import net.minecraft.util.Mth;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.render.ScissorState;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

//#if MC>=12109
//$$ import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

public abstract class AbstractScrollbar<P extends GuiScreen> extends AbstractScreenListener implements GuiWidget {

    protected final List<Entry> entries = Lists.newCopyOnWriteArrayList();
    protected final List<EntryPosition> entryPositions = Lists.newCopyOnWriteArrayList();

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

    private int lastMaxScroll = 0;

    public AbstractScrollbar(P parent,
                             int containerWidth,
                             int y0, int y1) {
        this.parent = parent;

        this.containerWidth = containerWidth;
        this.width = parent.getWidth();
        this.height = parent.getHeight();

        this.y0 = y0;
        this.y1 = y1;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        if (isMouseOver(mouseX, mouseY)) {
            this.hoveredEntry = getEntryAtPosition(mouseX, mouseY).orElse(null);
        } else {
            this.hoveredEntry = null;
        }

        int trackX0 = getScrollbarPosition();
        int trackX1 = trackX0 + 6;

        int maxScroll = getMaxScroll();
        if (lastMaxScroll != maxScroll) {
            if (lastMaxScroll > 0) {
                double scrollPercent = scrollTop / lastMaxScroll;
                setScrollTop(maxScroll * scrollPercent);
            } else {
                setScrollTop(0.0D);
            }
        }
        lastMaxScroll = maxScroll;

        // render list
        renderList(context, getContainerX0(), y0, mouseX, mouseY, delta);

        if (maxScroll > 0) {
            int scrollbarY0 = y0 + getScrollbarPadding();
            int scrollbarY1 = y1 - getScrollbarPadding();

            int trackBottom = (int) ((float) ((scrollbarY1 - scrollbarY0) * (scrollbarY1 - scrollbarY0)) / (float) scrollHeight);
            trackBottom = Mth.clamp(trackBottom, 32, scrollbarY1 - scrollbarY0 - 8);
            int trackTop = (int) scrollTop * (scrollbarY1 - scrollbarY0 - trackBottom) / maxScroll + scrollbarY0;
            if (trackTop < scrollbarY0) {
                trackTop = scrollbarY0;
            }

            if (shouldRenderScrollbarBackground()) {
                context.fill(trackX0, scrollbarY0, trackX1, scrollbarY1, new Color(0, 0, 0));
            }
            context.fill(trackX0, trackTop, trackX1, trackTop + trackBottom, new Color(128, 128, 128));
            context.fill(trackX0, trackTop, trackX1 - 1, trackTop + trackBottom - 1, new Color(192, 192, 192));
        }

        //#if MC>=12109
        //$$ if (isMouseOverScrollbar(mouseX, mouseY)) {
        //$$     context.requestCursor(scrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
        //$$ }
        //#endif
    }

    // GuiScreenListener impl
    @Override
    public List<? extends GuiWidgetListener> widgets() {
        return entries;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        boolean entryScrolled = entries.stream()
                .flatMap(entry -> entry.widgets().stream())
                .filter(entry -> entry.isMouseOver(mouseX, mouseY))
                .anyMatch(entry -> entry.mouseScrolled(mouseX, mouseY, delta));
        if (entryScrolled) {
            return true;
        }

        if (getMaxScroll() == 0) {
            return false;
        }

        setScrollTop(scrollTop - delta * ((float) scrollHeight / entries.size()));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && isMouseOverScrollbar(mouseX, mouseY);
        if (scrolling) {
            return true;
        }

        Optional<Entry> entry = getEntryAtPosition(mouseX, mouseY);

        if (!this.isMouseOver(mouseX, mouseY)) return false;

        if (entry.isPresent()) {
            if (entry.get().mouseClicked(mouseX, mouseY, button)) {
                setFocused(entry.get());
                setDragging(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrolling = false;

        if (getFocused() != null) {
            getFocused().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean entryDragged = entries.stream()
                .flatMap(entry -> entry.widgets().stream())
                .filter(entry -> entry.isMouseOver(mouseX, mouseY))
                .anyMatch(entry -> entry.mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        if (entryDragged) {
            return true;
        }

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
                int scrollStep = Mth.clamp((int)((float)(diff * diff) / (float)scrollHeight), 32, diff);
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
                mouseX >= getContainerX0() &&
                mouseX <= getContainerX1();
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
        this.scrollHeight = 0;
        entryPositions.clear();

        entries.forEach(entry -> {
            EntryPosition lastPosition = getLastEntryPosition();
            entryPositions.add(new EntryPosition(lastPosition.bottom, lastPosition.bottom + entry.getHeight()));

            this.scrollHeight += entry.getHeight();
        });
    }

    // ???
    public void setScrollTop(double scrollTop) {
        this.scrollTop = Mth.clamp(scrollTop, 0D, getMaxScroll());
    }

    protected void renderList(@NotNull GuiRenderContext context, int x, int y, int mouseX, int mouseY, float delta) {
        ScissorState scissorState = context.getScissorState();

        //#if MC>=12106
        //$$ ScissorState newScissorState = ScissorState.ofScaled(
        //$$         getContainerX0(),
        //$$         y0,
        //$$         containerWidth,
        //$$         y1 - y0
        //$$ );
        //#else
        ScissorState newScissorState = ScissorState.of(
                getContainerX0(),
                height - y1,
                containerWidth,
                y1 - y0
        );
        //#endif

        if (scissorState != null) {
            int parentX = scissorState.getX();
            int parentY = scissorState.getY();
            int parentRight = parentX + scissorState.getWidth();
            int parentTop = parentY + scissorState.getHeight();

            int childX = newScissorState.getX();
            int childY = newScissorState.getY();
            int childRight = childX + newScissorState.getWidth();
            int childTop = childY + newScissorState.getHeight();

            int clippedX = Math.max(childX, parentX);
            int clippedY = Math.max(childY, parentY);
            int clippedRight = Math.min(childRight, parentRight);
            int clippedTop = Math.min(childTop, parentTop);

            int clippedWidth = Math.max(0, clippedRight - clippedX);
            int clippedHeight = Math.max(0, clippedTop - clippedY);

            newScissorState = ScissorState.ofScaled(clippedX, clippedY, clippedWidth, clippedHeight);
        }

        context.applyScissorState(newScissorState);

        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            EntryPosition position = entryPositions.get(index);

            int entryTop = y - (int) scrollTop + position.top;
            int entryBottom = y - (int) scrollTop + position.bottom;

            entry.updatePosition(context, index, x, entryTop, containerWidth, mouseX, mouseY, Objects.equals(hoveredEntry, entry), delta);

            if (entryTop > y1 || entryBottom < y0) continue;

            entry.render(context, index, x, entryTop, containerWidth, mouseX, mouseY, Objects.equals(hoveredEntry, entry), delta);
        }

        context.applyScissorState(scissorState);
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

    protected int getScrollbarPadding() {
        return 4;
    }

    protected boolean shouldRenderScrollbarBackground() {
        return true;
    }

    protected final boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return getMaxScroll() > 0 &&
                mouseX >= this.getScrollbarPosition() &&
                mouseX < (this.getScrollbarPosition() + 6) &&
                mouseY >= y0 &&
                mouseY <= y1;
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

    protected final int getMaxScroll() {
        return Math.max(0, scrollHeight - (y1 - y0));
    }

    private @NotNull EntryPosition getLastEntryPosition() {
        if (entryPositions.size() == 0) return EntryPosition.EMPTY;
        return entryPositions.get(entryPositions.size() - 1);
    }

    public abstract void init();

    // widgets inside scrollbar are always Entry
    @SuppressWarnings("unchecked")
    public @Nullable GuiWidgetListener getFocusedWidget() {
        Entry entry = (Entry) getFocused();
        if (entry == null) return null;

        GuiWidgetListener widget = entry.getFocused();
        if (widget instanceof AbstractScrollbar<?>) {
            return ((AbstractScrollbar<?>) widget).getFocusedWidget();
        }

        if (widget instanceof GuiAbstractWidget) {
            GuiAbstractWidget abstractWidget = (GuiAbstractWidget) widget;
            if (!abstractWidget.isFocused()) return null;

            return widget;
        }

        return null;
    }

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
            if (this.height != height) {
                this.height = height;
                AbstractScrollbar.this.onEntryHeightChange();
            }
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return Collections.emptyList();
        }

        public void updatePosition(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
        }

        public abstract void render(@NotNull GuiRenderContext context, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta);
    }
}
