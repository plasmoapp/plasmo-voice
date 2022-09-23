package su.plo.voice.client.gui.settings.tab;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.components.IconButton;
import su.plo.lib.client.gui.widget.GuiAbstractWidget;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.line.ClientSourceLine;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.UpdatableWidget;
import su.plo.voice.client.gui.settings.widget.VolumeSliderWidget;
import su.plo.voice.config.entry.DoubleConfigEntry;

import java.util.Collections;
import java.util.List;

public final class VolumesTabWidget extends TabWidget {

    private final ClientSourceLineManager sourceLines;

    public VolumesTabWidget(@NotNull MinecraftClientLib minecraft,
                            @NotNull VoiceSettingsScreen parent,
                            @NotNull PlasmoVoiceClient voiceClient,
                            @NotNull ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.sourceLines = voiceClient.getSourceLineManager();
    }

    @Override
    public void init() {
        super.init();

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.volumes.sources"), 24));

        sourceLines.getLines().forEach(this::createVolume);

        addEntry(new CategoryEntry(TextComponent.translatable("gui.plasmovoice.volumes.players"), 24));

    }

    private void createVolume(@NotNull ClientSourceLine sourceLine) {
        DoubleConfigEntry volumeEntry = config.getVoice().getVolumes().getVolume(sourceLine.getName());
        ConfigEntry<Boolean> muteEntry = config.getVoice().getVolumes().getMute(sourceLine.getName());

        VolumeSliderWidget volumeSlider = new VolumeSliderWidget(
                minecraft,
                voiceClient.getKeyBindings(),
                volumeEntry,
                "%",
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        List<Button> buttons = Lists.newArrayList();

        Runnable updateButtons = () -> {
            buttons.get(0).setVisible(!muteEntry.value());
            buttons.get(1).setVisible(muteEntry.value());
        };

        Button.OnPress buttonClick = (button) -> {
            muteEntry.set(!muteEntry.value());
            updateButtons.run();
        };

        IconButton muteButton = new IconButton(
                minecraft,
                0,
                0,
                20,
                20,
                buttonClick,
                Button.NO_TOOLTIP,
                "plasmovoice:textures/icons/speaker.png",
                true
        );

        IconButton unmuteButton = new IconButton(
                minecraft,
                0,
                0,
                20,
                20,
                buttonClick,
                Button.NO_TOOLTIP,
                "plasmovoice:textures/icons/speaker_disabled.png",
                true
        );

        muteButton.setVisible(!muteEntry.value());
        unmuteButton.setVisible(muteEntry.value());

        buttons.add(muteButton);
        buttons.add(unmuteButton);

        addEntry(new SourceVolumeEntry<>(
                TextComponent.translatable(sourceLine.getTranslation()),
                volumeSlider,
                buttons,
                volumeEntry,
                muteEntry,
                Collections.emptyList(),
                sourceLine.getIcon(),
                (button, element) -> updateButtons.run()
        ));
    }


    class SourceVolumeEntry<W extends GuiAbstractWidget> extends ButtonOptionEntry<W> {

        private final String iconLocation;
        private final ConfigEntry<Boolean> muteEntry;

        public SourceVolumeEntry(@NotNull TextComponent text,
                                 @NotNull W widget,
                                 @NotNull List<Button> buttons,
                                 @NotNull ConfigEntry<?> entry,
                                 @NotNull ConfigEntry<Boolean> muteEntry,
                                 @NotNull List<TextComponent> tooltip,
                                 @NotNull String iconLocation,
                                 @NotNull OptionResetAction<W> resetAction) {
            super(text, widget, buttons, entry, tooltip, resetAction);

            this.muteEntry = muteEntry;
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
            return entry.isDefault() && muteEntry.isDefault();
        }

        @Override
        protected void onReset(@NotNull Button button) {
            entry.reset();
            muteEntry.reset();

            if (element instanceof UpdatableWidget)
                ((UpdatableWidget) element).updateValue();

            if (resetAction != null)
                resetAction.onReset(resetButton, element);
        }
    }
}
