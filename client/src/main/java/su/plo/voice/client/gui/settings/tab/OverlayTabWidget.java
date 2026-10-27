package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.BooleanConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.CompositeColumnWidget;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.config.IconPosition;
import su.plo.voice.api.client.config.overlay.OverlayPosition;
import su.plo.voice.api.client.config.overlay.OverlaySourceState;
import su.plo.voice.api.client.config.overlay.OverlayStyle;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
import su.plo.voice.client.gui.settings.widget.OptionWidget;
import su.plo.voice.client.gui.settings.widget.ToggleButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OverlayTabWidget extends TabWidget {

    private static final List<McTextComponent> ICONS_LIST = ImmutableList.of(
            McTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.hud"),
            McTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.always"),
            McTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.hidden")
    );

    private static final List<McTextComponent> OVERLAY_DISPLAYS = ImmutableList.of(
            McTextComponent.translatable("gui.plasmovoice.overlay.sources.when_talking"),
            McTextComponent.translatable("gui.plasmovoice.overlay.sources.always"),
            McTextComponent.translatable("gui.plasmovoice.overlay.sources.never")
    );

    private final ClientSourceLineManager sourceLines;

    public OverlayTabWidget(VoiceSettingsScreen parent,
                            PlasmoVoiceClient voiceClient,
                            VoiceClientConfig config) {
        super(parent, voiceClient, config);

        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();

        CompositeColumnWidget activationIcon = addCategory(McTextComponent.translatable("gui.plasmovoice.overlay.activation_icon"));
        activationIcon.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_show"),
                null,
                config.getOverlay().getShowActivationIcon()
        ));
        activationIcon.add(createActivationIconPosition());

        CompositeColumnWidget sourceIcons = addCategory(McTextComponent.translatable("gui.plasmovoice.overlay.source_icons"));
        sourceIcons.add(createShowIcons());
        sourceIcons.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.overlay.show_static_source_icons"),
                null,
                config.getOverlay().getShowStaticSourceIcons()
        ));

        CompositeColumnWidget overlay = addCategory(McTextComponent.translatable("gui.plasmovoice.overlay"));
        overlay.add(createToggleEntry(
                McTextComponent.translatable("gui.plasmovoice.overlay.enable"),
                null,
                config.getOverlay().getOverlayEnabled()
        ));
        overlay.add(createOverlayPosition());
        overlay.add(createOverlayStyle());

        CompositeColumnWidget sources = addCategory(McTextComponent.translatable("gui.plasmovoice.overlay.sources"));

        List<ClientSourceLine> sourceLines = Lists.newArrayList(this.sourceLines.getLines());
        Collections.reverse(sourceLines);
        sourceLines.forEach(sourceLine -> sources.add(createOverlaySource(sourceLine)));
    }

    private OptionWidget<?> createOverlaySource(@NotNull ClientSourceLine sourceLine) {
        EnumConfigEntry<OverlaySourceState> configEntry = config.getOverlay().getSourceStates().getState(sourceLine);
        GuiAbstractWidget label = iconLabel(
                ResourceLocation.tryParse(sourceLine.getIcon()),
                McTextComponent.translatable(sourceLine.getTranslation())
        );

        if (!sourceLine.hasPlayers()) {
            BooleanConfigEntry booleanConfigEntry = new BooleanConfigEntry(false);
            booleanConfigEntry.set(isShown(configEntry.value()));
            booleanConfigEntry.addChangeListener(state -> configEntry.set(OverlaySourceState.fromBoolean(state)));

            ToggleButton widget = new ToggleButton(
                    booleanConfigEntry,
                    0,
                    0,
                    ELEMENT_WIDTH,
                    20
            );

            return new OptionWidget<>(
                    parent,
                    label,
                    widget,
                    booleanConfigEntry
            );
        } else {
            DropDownWidget widget = new DropDownWidget(
                    parent,
                    0,
                    0,
                    ELEMENT_WIDTH,
                    20,
                    OVERLAY_DISPLAYS.get(overlayDisplayIndex(configEntry.value())),
                    OVERLAY_DISPLAYS,
                    true,
                    (index) -> {
                        switch (index) {
                            case 0:
                                configEntry.set(OverlaySourceState.WHEN_TALKING);
                                break;
                            case 1:
                                configEntry.set(OverlaySourceState.ALWAYS);
                                break;
                            case 2:
                                configEntry.set(OverlaySourceState.NEVER);
                                break;
                        }
                    }
            );

            return new OptionWidget<>(
                    parent,
                    label,
                    widget,
                    configEntry,
                    null,
                    (button, element) -> element.setText(OVERLAY_DISPLAYS.get(0))
            );
        }
    }

    private static boolean isShown(@NotNull OverlaySourceState state) {
        return state != OverlaySourceState.OFF && state != OverlaySourceState.NEVER;
    }

    private static int overlayDisplayIndex(@NotNull OverlaySourceState state) {
        switch (state) {
            case ALWAYS:
                return 1;
            case NEVER:
            case OFF:
                return 2;
            case ON:
            case WHEN_TALKING:
            default:
                return 0;
        }
    }

    private OptionWidget<DropDownWidget> createShowIcons() {
        DropDownWidget dropdown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                ICONS_LIST.get(config.getOverlay().getShowSourceIcons().value()),
                ICONS_LIST,
                true,
                (index) -> config.getOverlay().getShowSourceIcons().set(index)
        );

        return new OptionWidget<>(
                parent,
                McTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons"),
                dropdown,
                config.getOverlay().getShowSourceIcons(),
                null,
                (button, element) -> element.setText(ICONS_LIST.get(config.getOverlay().getShowSourceIcons().value()))
        );
    }

    private OptionWidget<Button> createActivationIconPosition() {
        Button button = new Button(
                0,
                0,
                ELEMENT_WIDTH,
                20,
                McTextComponent.translatable(config.getOverlay().getActivationIconPosition().value().getTranslation()),
                (btn) -> {
                    IconPosition disabledPosition = null;
                    try {
                        disabledPosition = IconPosition.valueOf(config.getOverlay().getOverlayPosition().value().toString());
                    } catch (IllegalArgumentException ignored) {
                    }

                    ScreenWrapper.openScreen(
                            new ActivationIconPositionScreen(
                                    parent,
                                    config.getOverlay().getActivationIconPosition(),
                                    disabledPosition
                            )
                    );
                },
                Button.NO_TOOLTIP
        );

        return new OptionWidget<>(
                parent,
                McTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_position"),
                button,
                config.getOverlay().getActivationIconPosition(),
                null,
                (btn, element) -> element.setText(
                        McTextComponent.translatable(config.getOverlay().getActivationIconPosition().value().getTranslation())
                )
        );
    }

    private OptionWidget<DropDownWidget> createOverlayStyle() {
        EnumConfigEntry<OverlayStyle> configEntry = config.getOverlay().getOverlayStyle();

        DropDownWidget dropDown = new DropDownWidget(
                parent,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                configEntry.value().getTranslatable(),
                Arrays.stream(OverlayStyle.values())
                        .map(OverlayStyle::getTranslatable)
                        .collect(Collectors.toList()),
                false,
                (index) -> configEntry.set(OverlayStyle.Companion.fromOrdinal(index))
        );

        return new OptionWidget<>(
                parent,
                McTextComponent.translatable("gui.plasmovoice.overlay.style"),
                dropDown,
                configEntry,
                null,
                (btn, element) -> element.setText(configEntry.value().getTranslatable())
        );
    }

    private OptionWidget<Button> createOverlayPosition() {
        Button button = new Button(
                0,
                0,
                ELEMENT_WIDTH,
                20,
                McTextComponent.translatable(config.getOverlay().getOverlayPosition().value().getTranslation()),
                (btn) -> {
                    OverlayPosition disabledPosition = null;
                    try {
                        disabledPosition = OverlayPosition.valueOf(
                                config.getOverlay().getActivationIconPosition().value().toString()
                        );
                    } catch (IllegalArgumentException ignored) {
                    }

                    ScreenWrapper.openScreen(
                            new OverlayPositionScreen(
                                    parent,
                                    config.getOverlay().getOverlayPosition(),
                                    disabledPosition
                            )
                    );
                },
                Button.NO_TOOLTIP
        );

        return new OptionWidget<>(
                parent,
                McTextComponent.translatable("gui.plasmovoice.overlay.position"),
                button,
                config.getOverlay().getOverlayPosition(),
                null,
                (btn, element) -> element.setText(
                        McTextComponent.translatable(config.getOverlay().getOverlayPosition().value().getTranslation())
                )
        );
    }
}
