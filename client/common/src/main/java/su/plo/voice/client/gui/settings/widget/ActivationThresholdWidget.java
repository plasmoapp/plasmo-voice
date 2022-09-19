package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.components.AbstractSlider;
import su.plo.lib.client.gui.components.Button;
import su.plo.lib.client.gui.components.IconButton;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.event.gui.MicrophoneTestStartedEvent;
import su.plo.voice.client.event.gui.MicrophoneTestStoppedEvent;
import su.plo.voice.client.gui.settings.MicrophoneTestController;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.config.entry.DoubleConfigEntry;

import java.util.List;

public final class ActivationThresholdWidget extends AbstractSlider implements UpdatableWidget {

    private static final String STOP_ICON = "plasmovoice:textures/icons/speaker.png";
    private static final String START_ICON = "plasmovoice:textures/icons/speaker_disabled_v1.png";
    private static final TextComponent NOT_AVAILABLE = TextComponent.translatable("gui.plasmovoice.devices.not_available");

    private final VoiceSettingsScreen parent;
    private final MicrophoneTestController controller;
    private final DoubleConfigEntry entry;
    private final List<IconButton> microphoneTest;

    public ActivationThresholdWidget(@NotNull MinecraftClientLib minecraft,
                                     @NotNull VoiceSettingsScreen parent,
                                     @NotNull MicrophoneTestController testController,
                                     @NotNull DeviceManager devices,
                                     @NotNull DoubleConfigEntry entry,
                                     int x,
                                     int y,
                                     int width,
                                     int height) {
        super(minecraft, x, y, width, height);

        this.parent = parent;
        this.controller = testController;
        this.entry = entry;

        IconButton testStop = new IconButton(
                minecraft,
                0,
                8,
                20,
                20,
                button -> controller.stop(),
                Button.NO_TOOLTIP,
                STOP_ICON,
                true
        );

        IconButton testStart = new IconButton(
                minecraft,
                0,
                20,
                20,
                20,
                button -> controller.start(),
                (button, matrices, mouseX, mouseY) -> {
                    if (!button.isActive()) {
                        parent.setTooltip(ImmutableList.of(NOT_AVAILABLE));
                    }
                },
                START_ICON,
                true
        );


        testStop.setVisible(false);
        testStart.setActive(devices.getDevices(DeviceType.INPUT).size() > 0);
        this.microphoneTest = ImmutableList.of(testStop, testStart);

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = TextComponent.literal(String.format("%.0f dB", AudioUtil.doubleRangeToAudioLevel(value)));
    }

    @Override
    protected void applyValue() {
        entry.set(AudioUtil.doubleRangeToAudioLevel(value));
    }

    @Override
    protected int getSliderWidth() {
        return width - 22;
    }

    @Override
    public void updateValue() {
        this.value = AudioUtil.audioLevelToDoubleRange(entry.value());
        updateText();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) ||
                microphoneTest.get(0).mouseClicked(mouseX, mouseY, button) ||
                microphoneTest.get(1).mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX >= x && mouseX <= x + width) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        } else {
            return false;
        }
    }

    @Override
    public boolean isHoveredOrFocused() {
        return super.isHoveredOrFocused() && active;
    }

    @Override
    public void renderButton(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        renderBackground(render, mouseX, mouseY);

        int sliderWidth = getSliderWidth();

        renderMicrophoneValue(render, sliderWidth);
        renderTrack(render, mouseX, mouseY);
        renderText(render, mouseX, mouseY);

        render.setShaderColor(1F, 1F, 1F, alpha);
        for (IconButton button : microphoneTest) {
            button.setX(x + sliderWidth + 2);
            button.setY(y);

            button.render(render, mouseX, mouseY, delta);
        }
    }

    private void renderMicrophoneValue(@NotNull GuiRender render, int sliderWidth) {
        if (controller.getMicrophoneValue() > 0.95D) {
            render.setShaderColor(1F, 0F, 0F, alpha);
        } else if (controller.getMicrophoneValue() > 0.7D) {
            render.setShaderColor(1F, 1F, 0F, alpha);
        } else {
            render.setShaderColor(0F, 1F, 0F, alpha);
        }

        render.setShaderTexture(0, WIDGETS_LOCATION);
        render.blit(x + 1, y + 1, 1, 47, (int) ((sliderWidth - 2) * controller.getMicrophoneValue()), height - 2);
    }

    @EventSubscribe
    public void onTestStarted(@NotNull MicrophoneTestStartedEvent event) {
        microphoneTest.get(0).setVisible(true);
        microphoneTest.get(1).setVisible(false);
    }

    @EventSubscribe
    public void onTestStopped(@NotNull MicrophoneTestStoppedEvent event) {
        microphoneTest.get(0).setVisible(false);
        microphoneTest.get(1).setVisible(true);
    }
}
