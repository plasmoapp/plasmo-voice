package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.GuiUtil;
import su.plo.lib.client.gui.components.AbstractScrollbar;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.components.IconButton;
import su.plo.lib.client.gui.components.TextFieldWidget;
import su.plo.lib.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.client.gui.widget.GuiWidgetListener;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.*;
import su.plo.voice.config.entry.DoubleConfigEntry;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class TabWidget extends AbstractScrollbar<VoiceSettingsScreen> {

    protected static final int ELEMENT_WIDTH = 124;

    protected final PlasmoVoiceClient voiceClient;
    protected final ClientConfig config;

    public TabWidget(@NotNull MinecraftClientLib minecraft,
                     @NotNull VoiceSettingsScreen parent,
                     @NotNull PlasmoVoiceClient voiceClient,
                     @NotNull ClientConfig config) {
        super(
                minecraft,
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

        // todo: use something like predicate in updateOptionEntries
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

        // todo: use something like predicate in updateOptionEntries
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
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        super.render(render, mouseX, mouseY, delta);
    }

    public void setTooltip(List<TextComponent> tooltip) {
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

    protected OptionEntry<ToggleButton> createToggleEntry(@NotNull String translatable,
                                                          @Nullable String tooltipTranslatable,
                                                          @NotNull ConfigEntry<Boolean> entry) {
        ToggleButton toggleButton = new ToggleButton(
                minecraft,
                entry,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable(translatable),
                toggleButton,
                entry,
                GuiUtil.multiLineTooltip(minecraft.getLanguage(), tooltipTranslatable)
        );
    }

    protected OptionEntry<VolumeSliderWidget> createVolumeSlider(@NotNull String translatable,
                                                                 @Nullable String tooltipTranslatable,
                                                                 @NotNull DoubleConfigEntry entry,
                                                                 @NotNull String suffix) {
        VolumeSliderWidget volumeSlider = new VolumeSliderWidget(
                minecraft,
                voiceClient.getKeyBindings(),
                entry,
                suffix,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable(translatable),
                volumeSlider,
                entry,
                GuiUtil.multiLineTooltip(minecraft.getLanguage(), tooltipTranslatable)
        );
    }

    protected OptionEntry<IntSliderWidget> createIntSliderWidget(@NotNull String translatable,
                                                                 @Nullable String tooltipTranslatable,
                                                                 @NotNull IntConfigEntry entry,
                                                                 @NotNull String suffix) {
        IntSliderWidget volumeSlider = new IntSliderWidget(
                minecraft,
                entry,
                suffix,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable(translatable),
                volumeSlider,
                entry,
                GuiUtil.multiLineTooltip(minecraft.getLanguage(), tooltipTranslatable)
        );
    }


    public final class CategoryEntry extends Entry {

        private static final int COLOR = 0xFFFFFF;

        private final TextComponent text;
        private final int textWidth;

        public CategoryEntry(@NotNull TextComponent text) {
            super(24);

            this.text = text;
            this.textWidth = minecraft.getFont().width(text);
        }

        public CategoryEntry(@NotNull TextComponent text, int height) {
            super(height);

            this.text = text;
            this.textWidth = minecraft.getFont().width(text);
        }

        @Override
        public void render(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            int elementX = x + (containerWidth / 2) - (minecraft.getFont().width(text) / 2);
            int elementY = y + (height / 2) - (minecraft.getFont().getLineHeight() / 2);

            render.drawString(text, elementX, elementY, COLOR);
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
        public void render(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            element.setX(x);
            element.setY(y);
            element.setWidth(entryWidth);
            element.render(render, mouseX, mouseY, delta);
        }

        @Override
        public List<? extends GuiWidgetListener> widgets() {
            return widgets;
        }
    }

    public class OptionEntry<W extends GuiAbstractWidget> extends Entry {

        protected final TextComponent text;
        protected final List<TextComponent> tooltip;
        protected final W element;
        protected final IconButton resetButton;
        protected final @Nullable TabWidget.OptionResetAction<W> resetAction;
        protected final ConfigEntry<?> entry;

        private final List<? extends GuiWidgetListener> widgets;

        public OptionEntry(@NotNull TextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @NotNull TabWidget.OptionResetAction<W> action) {
            this(text, widget, entry, Collections.emptyList(), action, 24);
        }

        public OptionEntry(@NotNull TextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @NotNull List<TextComponent> tooltip) {
            this(text, widget, entry, tooltip, null, 24);
        }

        public OptionEntry(@NotNull TextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry) {
            this(text, widget, entry, Collections.emptyList(), null, 24);
        }


        public OptionEntry(@NotNull TextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @NotNull List<TextComponent> tooltip,
                           @Nullable TabWidget.OptionResetAction<W> resetAction) {
            this(text, widget, entry, tooltip, resetAction, 24);
        }

        public OptionEntry(@NotNull TextComponent text,
                           @NotNull W widget,
                           @NotNull ConfigEntry<?> entry,
                           @NotNull List<TextComponent> tooltip,
                           @Nullable TabWidget.OptionResetAction<W> resetAction,
                           int height) {
            super(height);

            this.text = text;
            this.element = widget;
            this.entry = entry;
            this.tooltip = tooltip;
            this.resetAction = resetAction;

            this.resetButton = new IconButton(
                    minecraft,
                    0, 0,
                    20,
                    20,
                    this::onReset,
                    Button.NO_TOOLTIP,
                    "plasmovoice:textures/icons/speaker.png",
                    true
            );

            this.widgets = Lists.newArrayList(element, resetButton);
        }

        @Override
        public void render(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            renderText(render, index, x, y, entryWidth, mouseX, mouseY, hovered, delta);

            int elementY = y + height / 2 - element.getHeight() / 2;

            renderElement(render, index, x, y, entryWidth, mouseX, mouseY, hovered, delta, elementY);
            renderResetButton(render, index, x, y, entryWidth, mouseX, mouseY, hovered, delta, elementY);

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

        protected void renderText(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            render.drawString(text, x, y + height / 2 - minecraft.getFont().getLineHeight() / 2, 0xFFFFFF);
        }

        protected void renderElement(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta,
                                     int elementY) {
            element.setX(x + entryWidth - element.getWidth() - 24);
            element.setY(elementY);
            element.render(render, mouseX, mouseY, delta);
        }

        protected void renderResetButton(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta,
                                         int elementY) {
            resetButton.setX(x + entryWidth - 20);
            resetButton.setY(elementY);
            resetButton.setActive(entry != null && !isDefault());
            resetButton.setIconColor(!resetButton.isActive() ? -0x5f5f60 : 0);
            resetButton.render(render, mouseX, mouseY, delta);
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

        private final List<Button> buttons;
        private final List<GuiAbstractWidget> widgets;

        public ButtonOptionEntry(@NotNull TextComponent text,
                                 @NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @NotNull List<TextComponent> tooltip,
                                 @Nullable OptionResetAction<W> resetAction) {
            this(text, widget, buttons, entry, tooltip, resetAction, 24);
        }

        public ButtonOptionEntry(@NotNull TextComponent text,
                                 @NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @NotNull List<TextComponent> tooltip,
                                 @Nullable OptionResetAction<W> resetAction,
                                 int height) {
            super(text, widget, entry, tooltip, resetAction, height);

            this.buttons = buttons;
            this.widgets = Lists.newArrayList(element, resetButton);
            widgets.addAll(buttons);

            if (widgets.size() == 0) throw new IllegalArgumentException("buttons cannot be empty");
        }

        @Override
        protected void renderElement(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta, int elementY) {
            element.setX(x + entryWidth - element.getWidth() - 8 - resetButton.getWidth() - buttons.get(0).getWidth());
            element.setY(elementY);
            element.render(render, mouseX, mouseY, delta);

            for (Button button : buttons) {
                if (!button.isVisible()) continue;

                button.setX(x + entryWidth - button.getWidth() - 24);
                button.setY(elementY);
                button.render(render, mouseX, mouseY, delta);
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
