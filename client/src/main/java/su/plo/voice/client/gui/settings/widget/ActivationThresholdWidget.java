package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.DoubleConfigEntry;
import su.plo.lib.mod.client.gui.components.AbstractSlider;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.widget.GuiWidgetTexture;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.InputDevice;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.gui.settings.MicrophoneTestController;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

import java.awt.Color;
import java.util.List;

public final class ActivationThresholdWidget extends AbstractSlider implements UpdatableWidget {

    private static final ResourceLocation STOP_ICON = ResourceLocation.tryParse("plasmovoice:textures/icons/speaker_menu.png");
    private static final ResourceLocation START_ICON = ResourceLocation.tryParse("plasmovoice:textures/icons/speaker_menu_disabled.png");
    private static final McTextComponent NOT_AVAILABLE = McTextComponent.translatable("gui.plasmovoice.devices.not_available");

    private final DoubleConfigEntry entry;
    private final MicrophoneTestController controller;

    private final Button microphoneTest;

    public ActivationThresholdWidget(@NotNull VoiceSettingsScreen parent,
                                     @NotNull DoubleConfigEntry entry,
                                     @NotNull AudioCapture audioCapture,
                                     @NotNull DeviceManager devices,
                                     @NotNull MicrophoneTestController controller,
                                     int x,
                                     int y,
                                     int width,
                                     int height) {
        super(x, y, width, height);

        this.entry = entry;
        this.controller = controller;

        this.microphoneTest = new IconButton(
                0,
                8,
                20,
                20,
                button -> {
                    if (controller.isActive()) {
                        controller.stop();
                    } else {
                        controller.start();
                    }
                },
                (button, mouseX, mouseY) -> {
                    if (!button.isActive()) {
                        parent.setTooltip(NOT_AVAILABLE, mouseX, mouseY);
                    }
                },
                () -> controller.isActive() ? STOP_ICON : START_ICON,
                true
        );


        microphoneTest.setActive(
                audioCapture.getDevice()
                        .map(InputDevice::isOpen)
                        .orElse(false) &&
                        devices.getOutputDevice().isPresent()
        );

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = McTextComponent.literal(String.format("%.0f dB", AudioUtil.doubleRangeToAudioLevel(value)));
    }

    @Override
    protected void applyValue() {
        entry.set(AudioUtil.doubleRangeToAudioLevel(value));
    }

    @Override
    public void updateValue() {
        this.value = AudioUtil.audioLevelToDoubleRange(entry.value());
        updateText();
    }

    @Override
    protected double minStep() {
        return 1.0 / 60.0;
    }

    @Override
    public boolean isHoveredOrFocused() {
        return super.isHoveredOrFocused() && active;
    }

    @Override
    public void renderButton(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY);

        renderMicrophoneValue(context, getSliderWidth(), delta);
        renderTrack(context, mouseX, mouseY);
        renderText(context, mouseX, mouseY);
    }

    public List<Button> getButtons() {
        return ImmutableList.of(microphoneTest);
    }

    private void renderMicrophoneValue(@NotNull GuiRenderContext context, int sliderWidth, float delta) {
        Color color;
        if (controller.getMicrophoneValue() > 0.95D) {
            color = new Color(255, 0, 0);
        } else if (controller.getMicrophoneValue() > 0.7D) {
            color = new Color(255, 255, 0);
        } else {
            color = new Color(0, 255, 0);
        }

        context.blitColorSprite(
                GuiWidgetTexture.SLIDER,
                x + 1,
                y + 1,
                1,
                1,
                (int) ((sliderWidth - 2) * controller.getMicrophoneValue()),
                height - 2,
                Colors.withAlpha(color, alpha)
        );

        controller.tick(delta);
    }
}
