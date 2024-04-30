package su.plo.lib.mod.client.gui.screen;

import com.google.common.collect.Lists;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UMinecraft;
import gg.essential.universal.UResolution;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.widget.GuiNarrationWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;

import java.util.List;

public abstract class GuiScreen implements GuiWidget, GuiScreenListener {

    protected static final UMinecraft minecraft = UMinecraft.INSTANCE;

    private final List<GuiWidget> renderWidgets = Lists.newArrayList();
    private final List<GuiWidgetListener> widgets = Lists.newArrayList();
    private final List<GuiNarrationWidget> narrationWidgets = Lists.newArrayList();

    protected ScreenWrapper screen;

    @Getter
    @Setter
    private GuiWidgetListener focused;
    @Getter
    @Setter
    private boolean dragging;

    @Nullable
    private GuiNarrationWidget lastNarration;

    // GuiWidget impl
    @Override
    public void render(@NotNull UMatrixStack render, int mouseX, int mouseY, float delta) {
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
    public void setMinecraftScreen(@NotNull ScreenWrapper screen) {
        this.screen = screen;
    }

    public @NotNull ScreenWrapper getMinecraftScreen() {
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
//    public void updateNarratedWidget(@NotNull NarrationOutput narrationOutput) {
//        ImmutableList<GuiNarrationWidget> immutableList = narrationWidgets
//                .stream()
//                .filter(GuiNarrationWidget::isActive)
//                .collect(ImmutableList.toImmutableList());
//
//        NarrationSearchResult searchResult = findNarrationWidget(immutableList, lastNarration);
//        if (searchResult != null) {
//            if (searchResult.priority.isTerminal()) {
//                this.lastNarration = searchResult.entry;
//            }
//
//            if (immutableList.size() > 1) {
//                narrationOutput.add(
//                        NarrationOutput.Type.POSITION,
//                        MinecraftTextComponent.translatable("narrator.position.screen", searchResult.index + 1, immutableList.size())
//                );
//                if (searchResult.priority == GuiNarrationWidget.NarrationPriority.FOCUSED) {
//                    narrationOutput.add(
//                            NarrationOutput.Type.USAGE, MinecraftTextComponent.translatable("narration.component_list.usage")
//                    );
//                }
//            }
//
//            searchResult.entry.updateNarration(narrationOutput.nest());
//        }
//    }

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

    public boolean shouldCloseOnEsc() {
        return true;
    }

    // lifecycle
    public void tick() {
    }

    public void init() {
    }

    public void removed() {
    }

    // getters
    @Override
    public int getWidth() {
        return ((Screen) screen).width;
    }

    @Override
    public int getHeight() {
        return ((Screen) screen).height;
    }

    public MinecraftTextComponent getTitle() {
        return MinecraftTextComponent.empty();
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
