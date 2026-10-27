package su.plo.voice.client.gui.settings.tab;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.CompositeColumnWidget;
import su.plo.lib.mod.client.gui.components.CompositeRowWidget;
import su.plo.lib.mod.client.gui.components.ScrollContainer;
import su.plo.lib.mod.client.gui.components.TextFieldWidget;
import su.plo.lib.mod.client.gui.components.TextWidget;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class TabWidget extends ScrollContainer {

    protected static final int ELEMENT_WIDTH = 124;
    protected static final int CATEGORY_GAP = 8;
    protected static final int ELEMENT_GAP = 4;
    protected static final int PAGE_PADDING = 4;

    private static final int CONTENT_WIDTH = 303;
    private static final int SCROLL_STEP = 24;

    protected final VoiceSettingsScreen parent;
    protected final PlasmoVoiceClient voiceClient;
    protected final VoiceClientConfig config;

    private CompositeColumnWidget page;

    public TabWidget(@NotNull VoiceSettingsScreen parent,
                     @NotNull PlasmoVoiceClient voiceClient,
                     @NotNull VoiceClientConfig config) {
        super(SCROLL_STEP);

        this.parent = parent;
        this.voiceClient = voiceClient;
        this.config = config;
        setWidth(CONTENT_WIDTH);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= y &&
                mouseY <= y + height &&
                mouseX >= 0 &&
                mouseX <= parent.getWidth();
    }

    public void tick() {
        descendants().stream()
                .filter(widget -> widget instanceof TextFieldWidget)
                .forEach(widget -> ((TextFieldWidget) widget).tick());
    }

    public void init() {
        parent.setOverlay(null);
        this.page = new CompositeColumnWidget(CATEGORY_GAP, PAGE_PADDING, PAGE_PADDING);
        setContent(page);
        repositionElements();
    }

    public void repositionElements() {
        int top = parent.getNavigation().getHeight();

        setX((parent.getWidth() - CONTENT_WIDTH) / 2);
        setY(top);
        setHeight(parent.getHeight() - top);
    }

    public void removed() {
        setScrollTop(0D);
    }

    public void setTooltip(@Nullable McTextComponent tooltip, int mouseX, int mouseY) {
        parent.setTooltip(tooltip, mouseX, mouseY);
    }

    protected CompositeColumnWidget addCategory(@NotNull McTextComponent title, @NotNull GuiAbstractWidget... trailing) {
        CompositeColumnWidget category = addSection();
        category.add(new TitleWidget(title, Collections.emptyList(), Arrays.asList(trailing)));
        return category;
    }

    protected CompositeColumnWidget addSection() {
        return page.add(new CompositeColumnWidget(ELEMENT_GAP));
    }

    protected GuiAbstractWidget iconLabel(@NotNull ResourceLocation icon, @NotNull McTextComponent text) {
        return new CompositeRowWidget(ELEMENT_GAP, new IconWidget(icon), new TextWidget(text));
    }

    protected void updateOptionEntries(Predicate<UpdatableWidget> predicate) {
        descendants().stream()
                .filter(widget -> widget instanceof UpdatableWidget)
                .map(widget -> (UpdatableWidget) widget)
                .filter(predicate)
                .forEach(UpdatableWidget::updateValue);
    }

    protected <E extends Enum<E>> OptionWidget<DropDownWidget> createDropDownEntry(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull Class<E> enumClass,
            @NotNull List<McTextComponent> elements,
            @NotNull EnumConfigEntry<E> entry,
            boolean elementTooltip
    ) {
        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                elements.get(entry.value().ordinal()),
                elements,
                elementTooltip,
                selected -> entry.set(enumClass.getEnumConstants()[selected])
        );

        return new OptionWidget<>(
                parent,
                text,
                dropdown,
                entry,
                tooltip,
                (button, element) -> element.setText(elements.get(entry.getDefault().ordinal()))
        );
    }

    protected OptionWidget<ToggleButton> createToggleEntry(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull ConfigEntry<Boolean> entry
    ) {
        return createToggleEntry(text, tooltip, entry, null);
    }

    protected OptionWidget<ToggleButton> createToggleEntry(
            @NotNull McTextComponent text,
            @Nullable McTextComponent tooltip,
            @NotNull ConfigEntry<Boolean> entry,
            @Nullable ToggleButton.PressAction onPress
    ) {
        ToggleButton toggleButton = new ToggleButton(
                entry,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                onPress
        );

        return new OptionWidget<>(
                parent,
                text,
                toggleButton,
                entry,
                tooltip
        );
    }

    protected OptionWidget<VolumeSliderWidget> createVolumeSlider(
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

        return new OptionWidget<>(
                parent,
                text,
                volumeSlider,
                entry,
                tooltip
        );
    }

    protected OptionWidget<IntSliderWidget> createIntSliderWidget(
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

        return new OptionWidget<>(
                parent,
                text,
                volumeSlider,
                entry,
                tooltip
        );
    }
}
