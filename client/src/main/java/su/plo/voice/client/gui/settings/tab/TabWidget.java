package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import su.plo.slib.api.chat.component.McTextComponent;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.AbstractScrollbar;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.components.TextFieldWidget;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class TabWidget extends AbstractScrollbar<VoiceSettingsScreen> {

    protected static final int ELEMENT_WIDTH = 124;

    protected final PlasmoVoiceClient voiceClient;
    protected final VoiceClientConfig config;

    public TabWidget(@NotNull VoiceSettingsScreen parent,
                     @NotNull PlasmoVoiceClient voiceClient,
                     @NotNull VoiceClientConfig config) {
        super(
                parent,
                303,
//                0, parent.getWidth(),
                0, 0
        );

        this.voiceClient = voiceClient;
        this.config = config;
    }

    public void tick() {
        for (Entry entry : entries) {
            for (GuiWidgetListener widget : entry.widgets()) {
                if (widget instanceof TextFieldWidget) {
                    ((TextFieldWidget) widget).tick();
                }
            }
        }
    }

    @Override
    public void init() {
        clearEntries();
        this.y0 = parent.getNavigation().getHeight() + 4;
        this.y1 = parent.getHeight() - 4;
    }

    public void removed() {
        setScrollTop(0D);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 &&
                mouseX >= this.getScrollbarPosition() &&
                mouseX < (this.getScrollbarPosition() + 6);

        Optional<Entry> entry = getEntryAtPosition(mouseX, mouseY);

        for (Entry e : entries) {
            if (!entry.isPresent() || entry.get() != e) {
                if (e.widgets().size() < 1) continue;

                if (e.widgets().get(0) instanceof DropDownWidget
                        || e.widgets().get(0) instanceof HotKeyWidget) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }

                if (e.widgets().get(0) instanceof TextFieldWidget) {
                    if (e.mouseClicked(mouseX, mouseY, button)) {
                        this.setFocused(e);
                        this.setDragging(true);
                        return true;
                    }
                }
            }
        }

        if (!isMouseOver(mouseX, mouseY)) return false;

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
        Optional<Entry> entry = getEntryAtPosition(mouseX, mouseY);

        for (Entry e : entries) {
            if ((!entry.isPresent() || entry.get() != e) && e instanceof OptionEntry) {
                if (e.widgets().get(0) instanceof HotKeyWidget) {
                    e.mouseReleased(mouseX, mouseY, button);
                }
            }
        }

        if (getFocused() != null) {
            getFocused().mouseReleased(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        super.render(stack, mouseX, mouseY, delta);
    }

    public void setTooltip(@Nullable McTextComponent tooltip) {
        parent.setTooltip(tooltip);
    }

    protected void updateOptionEntries(Predicate<UpdatableWidget> predicate) {
        for (Entry entry : entries) {
            if (!(entry instanceof OptionEntry)) continue;

            for (GuiWidgetListener widget : entry.widgets()) {
                if (!(widget instanceof UpdatableWidget)) continue;
                UpdatableWidget updatableWidget = (UpdatableWidget) widget;
                if (!predicate.test(updatableWidget)) continue;

                updatableWidget.updateValue();
            }
        }
    }

    protected OptionEntry<ToggleButton> createToggleEntry(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull ConfigEntry<Boolean> entry
    ) {
        ToggleButton toggleButton = new ToggleButton(
                entry,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                text,
                toggleButton,
                entry,
                tooltip
        );
    }

    protected OptionEntry<VolumeSliderWidget> createVolumeSlider(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull DoubleConfigEntry entry,
            @NotNull String suffix
    ) {
        VolumeSliderWidget volumeSlider = new VolumeSliderWidget(
                voiceClient.getHotkeys(),
                entry,
                suffix,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                text,
                volumeSlider,
                entry,
                tooltip
        );
    }

    protected OptionEntry<IntSliderWidget> createIntSliderWidget(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull IntConfigEntry entry,
            @NotNull String suffix
    ) {
        IntSliderWidget volumeSlider = new IntSliderWidget(
                entry,
                suffix,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                text,
                volumeSlider,
                entry,
                tooltip
        );
    }


    public final class CategoryEntry extends Entry {

        private static final int COLOR = 0xFFFFFF;

        private final McTextComponent text;
        private final int textWidth;

        public CategoryEntry(@NotNull McTextComponent text) {
            super(24);

            this.text = text;
            this.textWidth = RenderUtil.getTextWidth(text);
        }

        public CategoryEntry(@NotNull McTextComponent text, int height) {
            super(height);

            this.text = text;
            this.textWidth = RenderUtil.getTextWidth(text);
        }

        @Override
        public void render(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            int elementX = x + (containerWidth / 2) - (RenderUtil.getTextWidth(text) / 2);
            int elementY = y + (height / 2) - (UGraphics.getFontHeight() / 2);

            RenderUtil.drawString(stack, text, elementX, elementY, COLOR);
        }
    }

    public final class FullWidthEntry<W extends GuiAbstractWidget> extends Entry {

        private final W element;

        private final List<? extends GuiWidgetListener> widgets;

        public FullWidthEntry(@NotNull W element) {
            this(element, 24);
        }

        public FullWidthEntry(@NotNull W element, int height) {
            super(height);

            this.element = element;
            this.widgets = ImmutableList.of(element);
        }

        @Override
        public void render(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            element.setX(x);
            element.setY(y);
            element.setWidth(entryWidth);
            element.render(stack, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return widgets;
        }
    }

    public class OptionEntry<W extends GuiAbstractWidget> extends Entry {

        protected final McTextComponent text;
        protected final McTextComponent tooltip;
        protected final W element;
        protected final IconButton resetButton;
        protected final @Nullable TabWidget.OptionResetAction<W> resetAction;
        protected final ConfigEntry<?> entry;

        private final List<? extends GuiWidgetListener> widgets;

        public OptionEntry(@NotNull McTextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @NotNull TabWidget.OptionResetAction<W> action) {
            this(text, widget, entry, null, action, 24);
        }

        public OptionEntry(@NotNull McTextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @Nullable McTextComponent tooltip) {
            this(text, widget, entry, tooltip, null, 24);
        }

        public OptionEntry(@NotNull McTextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry) {
            this(text, widget, entry, null, null, 24);
        }


        public OptionEntry(@NotNull McTextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @Nullable McTextComponent tooltip,
                           @Nullable TabWidget.OptionResetAction<W> resetAction) {
            this(text, widget, entry, tooltip, resetAction, 24);
        }

        public OptionEntry(@NotNull McTextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @Nullable McTextComponent tooltip,
                           @Nullable TabWidget.OptionResetAction<W> resetAction,
                           int height) {
            super(height);

            this.text = text;
            this.element = widget;
            this.entry = entry;
            this.tooltip = tooltip;
            this.resetAction = resetAction;

            this.resetButton = new IconButton(
                    0, 0,
                    20,
                    20,
                    this::onReset,
                    Button.NO_TOOLTIP,
                    new ResourceLocation("plasmovoice:textures/icons/reset.png"),
                    true
            );

            this.widgets = Lists.newArrayList(element, resetButton);
        }

        @Override
        public void render(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            renderText(stack, index, x, y, entryWidth, mouseX, mouseY, hovered, delta);

            int elementY = y + height / 2 - element.getHeight() / 2;

            renderElement(stack, index, x, y, entryWidth, mouseX, mouseY, hovered, delta, elementY);
            renderResetButton(stack, index, x, y, entryWidth, mouseX, mouseY, hovered, delta, elementY);

            if (hovered && mouseX < element.getX()) {
                setTooltip(tooltip);
            }
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            for (GuiWidgetListener entry : widgets()) {
                if (entry instanceof HotKeyWidget && entry.mouseReleased(mouseX, mouseY, button)) {
                    return true;
                }
            }

            return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return widgets;
        }

        protected void renderText(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            RenderUtil.drawString(
                    stack,
                    text,
                    x,
                    y + height / 2 - UGraphics.getFontHeight() / 2,
                    0xFFFFFF
            );
        }

        protected void renderElement(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta,
                                     int elementY) {
            element.setX(x + entryWidth - element.getWidth() - 24);
            element.setY(elementY);
            element.render(stack, mouseX, mouseY, delta);
        }

        protected void renderResetButton(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta,
                                         int elementY) {
            resetButton.setX(x + entryWidth - 20);
            resetButton.setY(elementY);
            resetButton.setActive(entry != null && !isDefault());
            resetButton.render(stack, mouseX, mouseY, delta);
        }

        protected boolean isDefault() {
            return entry.isDefault();
        }

        protected void onReset(@NotNull Button button) {
            if (entry == null) return;
            entry.reset();

            if (element instanceof UpdatableWidget)
                ((UpdatableWidget) element).updateValue();

            if (resetAction != null)
                resetAction.onReset((IconButton) button, element);
        }
    }

    public class ButtonOptionEntry<W extends GuiAbstractWidget> extends OptionEntry<W> {

        protected final List<GuiAbstractWidget> widgets;

        protected final List<Button> buttons;

        public ButtonOptionEntry(@NotNull McTextComponent text,
                                 @NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @Nullable McTextComponent tooltip,
                                 @Nullable OptionResetAction<W> resetAction) {
            this(text, widget, buttons, entry, tooltip, resetAction, 24);
        }

        public ButtonOptionEntry(@NotNull McTextComponent text,
                                 @NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @Nullable McTextComponent tooltip,
                                 @Nullable OptionResetAction<W> resetAction,
                                 int height) {
            super(text, widget, entry, tooltip, resetAction, height);

            this.buttons = buttons;
            this.widgets = Lists.newArrayList(element, resetButton);
            widgets.addAll(buttons);
        }

        @Override
        protected void renderElement(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta, int elementY) {
            if (buttons.size() == 0) {
                super.renderElement(stack, index, x, y, entryWidth, mouseX, mouseY, hovered, delta, elementY);
                return;
            }

            List<Button> visibleButtons = buttons.stream()
                    .filter(Button::isVisible)
                    .collect(Collectors.toList());

            int visibleButtonsWidth = visibleButtons.stream()
                    .map(Button::getWidth)
                    .reduce(0, Integer::sum);

            element.setX(x + entryWidth - element.getWidth() - (visibleButtons.size() * 4) - resetButton.getWidth() - 4 - visibleButtonsWidth);
            element.setY(elementY);
            element.render(stack, mouseX, mouseY, delta);

            for (int i = 0; i < visibleButtons.size(); i++) {
                Button button = visibleButtons.get(i);

                button.setX(x + entryWidth - button.getWidth() - ((i + 1) * 24) - (i * 8));
                button.setY(elementY);
                button.render(stack, mouseX, mouseY, delta);
            }
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return widgets;
        }
    }

    interface OptionResetAction<T extends GuiAbstractWidget> {

        void onReset(@NotNull IconButton resetButton, T element);
    }
}
