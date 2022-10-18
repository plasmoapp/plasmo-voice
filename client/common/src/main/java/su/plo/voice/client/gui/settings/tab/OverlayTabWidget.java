package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.Button;
import su.plo.lib.api.client.gui.widget.GuiAbstractWidget;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.IconPosition;
import su.plo.voice.client.config.overlay.OverlayPosition;
import su.plo.voice.client.config.overlay.OverlaySourceState;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.DropDownWidget;
import su.plo.voice.client.gui.settings.widget.OverlaySourceStateButton;
import su.plo.voice.client.gui.settings.widget.UpdatableWidget;

import java.util.Collections;
import java.util.List;

public final class OverlayTabWidget extends TabWidget {

    private static final List<MinecraftTextComponent> ICONS_LIST = ImmutableList.of(
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.hud"),
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.always"),
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons.hidden")
    );

    private static final List<MinecraftTextComponent> OVERLAY_DISPLAYS = ImmutableList.of(
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.sources.when_talking"),
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.sources.always"),
            MinecraftTextComponent.translatable("gui.plasmovoice.overlay.sources.never")
    );

    private final ClientSourceLineManager sourceLines;

    public OverlayTabWidget(MinecraftClientLib minecraft,
                            VoiceSettingsScreen parent,
                            PlasmoVoiceClient voiceClient,
                            ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(createCategoryEntry("gui.plasmovoice.overlay.activation_icon"));
        addEntry(createToggleEntry(
                "gui.plasmovoice.overlay.activation_icon_show",
                null,
                config.getOverlay().getShowActivationIcon()
        ));
        addEntry(createActivationIconPosition());

        addEntry(createCategoryEntry("gui.plasmovoice.overlay.source_icons"));
        addEntry(createShowIcons());
        addEntry(createToggleEntry(
                "gui.plasmovoice.overlay.show_static_source_icons",
                null,
                config.getOverlay().getShowStaticSourceIcons()
        ));

        addEntry(createCategoryEntry("gui.plasmovoice.overlay"));
        addEntry(createToggleEntry(
                "gui.plasmovoice.overlay.enable",
                null,
                config.getOverlay().getOverlayEnabled()
        ));
        addEntry(createOverlayPosition());

        addEntry(createCategoryEntry("gui.plasmovoice.overlay.sources"));

        List<ClientSourceLine> sourceLines = Lists.newArrayList(this.sourceLines.getLines());
        Collections.reverse(sourceLines);
        sourceLines.forEach(this::createOverlaySource);
    }

    private void createOverlaySource(@NotNull ClientSourceLine sourceLine) {
        EnumConfigEntry<OverlaySourceState> configEntry = config.getOverlay().getSourceStates().getState(sourceLine);

        GuiAbstractWidget widget;
        if (!sourceLine.hasPlayers()) {
            widget = new OverlaySourceStateButton(
                    minecraft,
                    configEntry,
                    0,
                    0,
                    ELEMENT_WIDTH,
                    20
            );
        } else {
            widget = new DropDownWidget(
                    minecraft,
                    parent,
                    0,
                    0,
                    ELEMENT_WIDTH,
                    20,
                    OVERLAY_DISPLAYS.get(configEntry.value().ordinal() - 2),
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
        }

        addEntry(new OverlaySourceEntry<>(
                MinecraftTextComponent.translatable(sourceLine.getTranslation()),
                widget,
                configEntry,
                ImmutableList.of(),
                sourceLine.getIcon()
        ));
    }

    private OptionEntry<DropDownWidget> createShowIcons() {
        DropDownWidget dropdown = new DropDownWidget(
                minecraft,
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

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons"),
                dropdown,
                config.getOverlay().getShowSourceIcons(),
                (button, element) -> element.setText(ICONS_LIST.get(config.getOverlay().getShowSourceIcons().value()))
        );
    }

    private OptionEntry<Button> createActivationIconPosition() {
        Button button = new Button(
                minecraft,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                MinecraftTextComponent.translatable(config.getOverlay().getActivationIconPosition().value().getTranslation()),
                (btn) -> {
                    IconPosition disabledPosition = null;
                    try {
                        disabledPosition = IconPosition.valueOf(config.getOverlay().getOverlayPosition().value().toString());
                    } catch (IllegalArgumentException ignored) {
                    }

                    minecraft.setScreen(new ActivationIconPositionScreen(
                            minecraft,
                            parent,
                            config.getOverlay().getActivationIconPosition(),
                            disabledPosition
                    ));
                },
                Button.NO_TOOLTIP
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_position"),
                button,
                config.getOverlay().getActivationIconPosition(),
                (btn, element) -> element.setText(
                        MinecraftTextComponent.translatable(config.getOverlay().getActivationIconPosition().value().getTranslation())
                )
        );
    }

    private OptionEntry<Button> createOverlayPosition() {
        Button button = new Button(
                minecraft,
                0,
                0,
                ELEMENT_WIDTH,
                20,
                MinecraftTextComponent.translatable(config.getOverlay().getOverlayPosition().value().getTranslation()),
                (btn) -> {
                    OverlayPosition disabledPosition = null;
                    try {
                        disabledPosition = OverlayPosition.valueOf(
                                config.getOverlay().getActivationIconPosition().value().toString()
                        );
                    } catch (IllegalArgumentException ignored) {
                    }

                    minecraft.setScreen(new OverlayPositionScreen(
                            minecraft,
                            parent,
                            config.getOverlay().getOverlayPosition(),
                            disabledPosition
                    ));
                },
                Button.NO_TOOLTIP
        );

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.position"),
                button,
                config.getOverlay().getOverlayPosition(),
                (btn, element) -> element.setText(
                        MinecraftTextComponent.translatable(config.getOverlay().getOverlayPosition().value().getTranslation())
                )
        );
    }

    class OverlaySourceEntry<W extends GuiAbstractWidget> extends OptionEntry<W> {

        private final String iconLocation;

        public OverlaySourceEntry(@NotNull MinecraftTextComponent text,
                                  @NotNull W widget,
                                  @NotNull ConfigEntry<?> entry,
                                  @NotNull List<MinecraftTextComponent> tooltip,
                                  @NotNull String iconLocation) {
            super(text, widget, entry, tooltip, null);

            this.iconLocation = iconLocation;
        }

        @Override
        protected void renderText(@NotNull GuiRender render, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            render.setShaderTexture(0, iconLocation);
            render.setShaderColor(1F, 1F, 1F, 1F);
            render.blit(x, y + height / 2 - 8, 0, 0, 16, 16, 16, 16);

            render.drawString(text, x + 20, y + height / 2 - minecraft.getFont().getLineHeight() / 2, 0xFFFFFF);
        }

        @Override
        protected boolean isDefault() {
            return entry.isDefault();
        }

        @Override
        protected void onReset(@NotNull Button button) {
            entry.reset();

            if (element instanceof UpdatableWidget)
                ((UpdatableWidget) element).updateValue();

            if (resetAction != null)
                resetAction.onReset(resetButton, element);
        }
    }
}
