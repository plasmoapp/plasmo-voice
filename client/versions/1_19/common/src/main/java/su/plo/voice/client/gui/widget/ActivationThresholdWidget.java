package su.plo.voice.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.device.DeviceType;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.util.AudioUtil;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.event.gui.MicrophoneTestStartedEvent;
import su.plo.voice.client.event.gui.MicrophoneTestStoppedEvent;
import su.plo.voice.client.gui.MicrophoneTestController;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.config.entry.DoubleConfigEntry;

import java.util.List;

public final class ActivationThresholdWidget extends AbstractSliderButton implements UpdatableWidget {

    private final Minecraft minecraft;
    private final MicrophoneTestController controller;
    private final DoubleConfigEntry configEntry;
    private final boolean slider;
    private final List<IconButton> microphoneTest;

    public ActivationThresholdWidget(Minecraft minecraft,
                                     VoiceSettingsScreen parent,
                                     MicrophoneTestController controller,
                                     ClientConfig config,
                                     DeviceManager devices,
                                     int x,
                                     int y,
                                     int width,
                                     boolean slider) {
        super(x, y, width - 23, 20, Component.empty(), 0.0D);

        this.controller = controller;
        this.configEntry = config.getVoice().getActivationThreshold();
        this.minecraft = minecraft;
        this.slider = slider;
        this.updateValue();

        IconButton testStop = new IconButton(
                0,
                8,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/speaker.png"),
                button -> controller.stop(),
                Button.NO_TOOLTIP
        );

        IconButton testStart = new IconButton(
                0,
                0,
                20,
                20,
                new ResourceLocation("plasmovoice", "textures/icons/speaker_disabled_v1.png"),
                button -> controller.start(),
                (button, matrices, mouseX, mouseY) -> {
                    if (!button.isActive()) {
                        parent.setTooltip(ImmutableList.of(
                                Component.translatable("gui.plasmovoice.devices.not_available")
                        ));
                    }
                }
        );


        testStop.visible = false;
        testStart.active = devices.getDevices(DeviceType.INPUT).size() > 0;
        this.microphoneTest = ImmutableList.of(testStop, testStart);
    }

    @Override
    public void updateValue() {
        this.value = AudioUtil.audioLevelToDoubleRange(configEntry.value());
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage(Component.literal(String.format("%.0f dB", AudioUtil.doubleRangeToAudioLevel(value))));
    }

    protected void applyValue() {
        configEntry.set(AudioUtil.doubleRangeToAudioLevel(value));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return (slider && super.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return (slider && super.mouseClicked(mouseX, mouseY, button)) ||
                this.microphoneTest.get(0).mouseClicked(mouseX, mouseY, button) ||
                this.microphoneTest.get(1).mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (mouseX >= this.x && mouseX <= this.x + this.width && slider) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        } else {
            return false;
        }
    }

    @Override
    public boolean isHoveredOrFocused() {
        return super.isHoveredOrFocused() && this.active;
    }

    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        Font textRenderer = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        if (controller.getMicrophoneValue() > 0.95D) {
            RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, this.alpha);
        } else if (controller.getMicrophoneValue() > 0.7D) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, this.alpha);
        } else {
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, this.alpha);
        }
        blit(matrices, this.x + 1, this.y + 1, 1, 47, (int) ((this.width - 2) * controller.getMicrophoneValue()), this.height - 2);

        if (slider) {
            this.renderBg(matrices, minecraft, mouseX, mouseY);
            int j = this.active ? 16777215 : 10526880;
            drawCenteredString(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        for (IconButton button : this.microphoneTest) {
            button.x = this.x + this.width + 2;
            button.y = this.y;

            button.render(matrices, mouseX, mouseY, delta);
        }
    }

    @EventSubscribe
    public void onTestStarted(MicrophoneTestStartedEvent event) {
        microphoneTest.get(0).visible = true;
        microphoneTest.get(1).visible = false;
    }

    @EventSubscribe
    public void onTestStopped(MicrophoneTestStoppedEvent event) {
        microphoneTest.get(0).visible = false;
        microphoneTest.get(1).visible = true;
    }
}
