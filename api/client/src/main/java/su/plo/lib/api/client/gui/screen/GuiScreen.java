package su.plo.lib.api.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.MinecraftFont;
import su.plo.lib.api.client.gui.narration.NarrationOutput;
import su.plo.lib.api.client.gui.widget.GuiNarrationWidget;
import su.plo.lib.api.client.gui.widget.GuiWidget;
import su.plo.lib.api.client.gui.widget.GuiWidgetListener;

import java.util.List;

public abstract class GuiScreen implements GuiWidget, GuiScreenListener {

    protected final MinecraftClientLib minecraft;
    protected final MinecraftFont font;

    private final List<GuiWidget> renderWidgets = Lists.newArrayList();
    private final List<GuiWidgetListener> widgets = Lists.newArrayList();
    private final List<GuiNarrationWidget> narrationWidgets = Lists.newArrayList();

    protected MinecraftScreen screen;

    @Getter
    @Setter
    private GuiWidgetListener focused;
    @Getter
    @Setter
    private boolean dragging;

    @Nullable
    private GuiNarrationWidget lastNarration;

    public GuiScreen(@NotNull MinecraftClientLib minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.getFont();
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        for (GuiWidget widget : renderWidgets) {
            widget.render(render, mouseX, mouseY, delta);
        }
    }

    // GuiScreenEventListener impl
    @Override
    public List<? extends GuiWidgetListener> widgets() {
        return widgets;
    }

    // mc screen
    public void setMinecraftScreen(@NotNull MinecraftScreen screen) {
        this.screen = screen;
    }

    public @NotNull MinecraftScreen getMinecraftScreen() {
        return screen;
    }

    // widgets
    public <T extends GuiWidgetListener & GuiWidget> T addRenderWidget(T widget) {
        renderWidgets.add(widget);
        return addWidget(widget);
    }

    public <T extends GuiWidget> T addRenderOnlyWidget(T widget) {
        renderWidgets.add(widget);
        return widget;
    }

    public <T extends GuiWidgetListener> T addWidget(T widget) {
        widgets.add(widget);
        if (widget instanceof GuiNarrationWidget) {
            narrationWidgets.add((GuiNarrationWidget) widget);
        }
        return widget;
    }

    public void removeWidget(GuiWidgetListener widget) {
        if (widget instanceof GuiWidget) {
            renderWidgets.remove(widget);
        }

        widgets.remove(widget);
    }

    public void clearWidgets() {
        renderWidgets.clear();
        widgets.clear();
    }

    // narrations
    public void updateNarratedWidget(@NotNull NarrationOutput narrationOutput) {
        ImmutableList<GuiNarrationWidget> immutableList = narrationWidgets
                .stream()
                .filter(GuiNarrationWidget::isActive)
                .collect(ImmutableList.toImmutableList());

        NarrationSearchResult searchResult = findNarrationWidget(immutableList, lastNarration);
        if (searchResult != null) {
            if (searchResult.priority.isTerminal()) {
                this.lastNarration = searchResult.entry;
            }

            if (immutableList.size() > 1) {
                narrationOutput.add(
                        NarrationOutput.Type.POSITION,
                        TextComponent.translatable("narrator.position.screen", searchResult.index + 1, immutableList.size())
                );
                if (searchResult.priority == GuiNarrationWidget.NarrationPriority.FOCUSED) {
                    narrationOutput.add(
                            NarrationOutput.Type.USAGE, TextComponent.translatable("narration.component_list.usage")
                    );
                }
            }

            searchResult.entry.updateNarration(narrationOutput.nest());
        }
    }

    @Nullable
    public GuiScreen.NarrationSearchResult findNarrationWidget(List<? extends GuiNarrationWidget> list,
                                                               @Nullable GuiNarrationWidget entry) {
        NarrationSearchResult searchResult = null;
        NarrationSearchResult searchResult1 = null;
        int index = 0;

        for (int j = list.size(); index < j; ++index) {
            GuiNarrationWidget entry2 = list.get(index);
            GuiNarrationWidget.NarrationPriority narrationPriority = entry2.narrationPriority();
            if (narrationPriority.isTerminal()) {
                if (entry2 != entry) {
                    return new NarrationSearchResult(entry2, index, narrationPriority);
                }

                searchResult1 = new NarrationSearchResult(entry2, index, narrationPriority);
            } else if (narrationPriority.compareTo(searchResult != null ? searchResult.priority : GuiNarrationWidget.NarrationPriority.NONE) > 0) {
                searchResult = new NarrationSearchResult(entry2, index, narrationPriority);
            }
        }

        return searchResult != null ? searchResult : searchResult1;
    }


    // lifecycle
    public void tick() {
    }

    public void init() {
    }

    public void removed() {
    }

    public boolean onClose() {
        return false;
    }

    // getters
    public int getWidth() {
        return screen.getWidth();
    }

    public int getHeight() {
        return screen.getHeight();
    }

    public TextComponent getTitle() {
        return TextComponent.empty();
    }

    public static class NarrationSearchResult {

        public final GuiNarrationWidget entry;
        public final int index;
        public final GuiNarrationWidget.NarrationPriority priority;

        public NarrationSearchResult(GuiNarrationWidget entry,
                                     int index,
                                     GuiNarrationWidget.NarrationPriority narrationPriority) {
            this.entry = entry;
            this.index = index;
            this.priority = narrationPriority;
        }
    }
}
