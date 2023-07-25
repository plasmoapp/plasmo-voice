package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UMatrixStack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.EnumConfigEntry;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.gui.widget.GuiAbstractWidget;
import su.plo.lib.mod.client.render.RenderUtil;
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
import su.plo.voice.client.gui.settings.widget.OverlaySourceStateButton;
import su.plo.voice.client.gui.settings.widget.UpdatableWidget;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public OverlayTabWidget(VoiceSettingsScreen parent,
                            PlasmoVoiceClient voiceClient,
                            VoiceClientConfig config) {
        super(parent, voiceClient, config);

        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.overlay.activation_icon")));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_show"),
                null,
                config.getOverlay().getShowActivationIcon()
        ));
        addEntry(createActivationIconPosition());

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.overlay.source_icons")));
        addEntry(createShowIcons());
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_static_source_icons"),
                null,
                config.getOverlay().getShowStaticSourceIcons()
        ));

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.overlay")));
        addEntry(createToggleEntry(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.enable"),
                null,
                config.getOverlay().getOverlayEnabled()
        ));
        addEntry(createOverlayPosition());
        addEntry(createOverlayStyle());

        addEntry(new CategoryEntry(MinecraftTextComponent.translatable("gui.plasmovoice.overlay.sources")));

        List<ClientSourceLine> sourceLines = Lists.newArrayList(this.sourceLines.getLines());
        Collections.reverse(sourceLines);
        sourceLines.forEach(this::createOverlaySource);
    }

    private void createOverlaySource(@NotNull ClientSourceLine sourceLine) {
        EnumConfigEntry<OverlaySourceState> configEntry = config.getOverlay().getSourceStates().getState(sourceLine);

        if (!sourceLine.hasPlayers()) {
            OverlaySourceStateButton widget = new OverlaySourceStateButton(
                    configEntry,
                    0,
                    0,
                    ELEMENT_WIDTH,
                    20
            );

            addEntry(new OverlaySourceEntry<>(
                    MinecraftTextComponent.translatable(sourceLine.getTranslation()),
                    widget,
                    configEntry,
                    null,
                    new ResourceLocation(sourceLine.getIcon()),
                    null
            ));
        } else {
            DropDownWidget widget = new DropDownWidget(
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

            addEntry(new OverlaySourceEntry<>(
                    MinecraftTextComponent.translatable(sourceLine.getTranslation()),
                    widget,
                    configEntry,
                    null,
                    new ResourceLocation(sourceLine.getIcon()),
                    (button, element) -> element.setText(OVERLAY_DISPLAYS.get(0))
            ));
        }
    }

    private OptionEntry<DropDownWidget> createShowIcons() {
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

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.show_source_icons"),
                dropdown,
                config.getOverlay().getShowSourceIcons(),
                (button, element) -> element.setText(ICONS_LIST.get(config.getOverlay().getShowSourceIcons().value()))
        );
    }

    private OptionEntry<Button> createActivationIconPosition() {
        Button button = new Button(
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

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.activation_icon_position"),
                button,
                config.getOverlay().getActivationIconPosition(),
                (btn, element) -> element.setText(
                        MinecraftTextComponent.translatable(config.getOverlay().getActivationIconPosition().value().getTranslation())
                )
        );
    }

    private OptionEntry<DropDownWidget> createOverlayStyle() {
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

        return new OptionEntry<>(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay.style"),
                dropDown,
                configEntry,
                (btn, element) -> element.setText(configEntry.value().getTranslatable())
        );
    }

    private OptionEntry<Button> createOverlayPosition() {
        Button button = new Button(
                0,
                0,
                ELEMENT_WIDTH,
                20,
                MinecraftTextComponent.translatable(config.getOverlay().getOverlayPosition().value().getTranslation()),
                (btn) -> {
                    OverlayPosition disabledPosition = null;
                    try {
                        disabledPosition = OverlayPosition.valueOf(
                                config.getOverlay().getOverlayPosition().value().toString()
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

        private final ResourceLocation iconLocation;

        public OverlaySourceEntry(@NotNull MinecraftTextComponent text,
                                  @NotNull W widget,
                                  @NotNull ConfigEntry<?> entry,
                                  @Nullable MinecraftTextComponent tooltip,
                                  @NotNull ResourceLocation iconLocation,
                                  @Nullable OptionResetAction<W> resetAction) {
            super(text, widget, entry, tooltip, resetAction);

            this.iconLocation = iconLocation;
        }

        @Override
        protected void renderText(@NotNull UMatrixStack stack, int index, int x, int y, int entryWidth, int mouseX, int mouseY, boolean hovered, float delta) {
            UGraphics.bindTexture(0, iconLocation);
            UGraphics.color4f(1F, 1F, 1F, 1F);

            UGraphics.enableBlend();
            RenderUtil.blit(stack, x, y + height / 2 - 8, 0, 0, 16, 16, 16, 16);
            UGraphics.disableBlend();

            RenderUtil.drawString(
                    stack,
                    text,
                    x + 20,
                    y + height / 2 - UGraphics.getFontHeight() / 2,
                    0xFFFFFF
            );
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
