package su.plo.voice.client.gui.widgets;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.sound.openal.CustomSource;

import java.util.List;

public class MicrophoneThresholdWidget extends AbstractSliderButton {
    private final VoiceSettingsScreen parent;
    private final Minecraft client = Minecraft.getInstance();
    private final boolean slider;
    private List<BackgroundImageButton> microphoneTest;

    public MicrophoneThresholdWidget(int x, int y, int width, boolean slider, VoiceSettingsScreen parent) {
        super(x, y, width - 23, 20, TextComponent.EMPTY, 0.0D);
        this.slider = slider;
        this.updateValue();

        BackgroundImageButton speakerHide = new BackgroundImageButton(0, 0, 20, 20, 0, 72, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            closeSpeaker();
        });

        BackgroundImageButton speakerShow = new BackgroundImageButton(0, 0, 20, 20, 20, 72, 20,
                VoiceClient.ICONS, 256, 256, button -> {
            this.microphoneTest.get(0).visible = true;
            this.microphoneTest.get(1).visible = false;

            VoiceClient.getSoundEngine().runInContext(() -> {
                CustomSource source = VoiceClient.getSoundEngine().createSource();
                source.setRelative(true);
                parent.setSource(source);
            });
        });

        speakerHide.visible = false;
        speakerShow.active = VoiceClient.recorder.isAvailable();
        this.parent = parent;
        this.microphoneTest = ImmutableList.of(speakerHide, speakerShow);
    }

    public void closeSpeaker() {
        this.microphoneTest.get(0).visible = false;
        this.microphoneTest.get(1).visible = true;

        if (parent.getSource() != null) {
            VoiceClient.getSoundEngine().runInContext(() -> {
                parent.getSource().close();
                parent.setSource(null);
            });
        }
    }

    public void updateValue() {
        this.value = 1 - (Math.max(-60, VoiceClient.getClientConfig().voiceActivationThreshold.get()) / -60);
        this.updateMessage();
    }

    protected void updateMessage() {
        this.setMessage(new TextComponent(Math.round((1 - this.value) * -60) + " dB"));
    }

    protected void applyValue() {
        VoiceClient.getClientConfig().voiceActivationThreshold.set((double) Math.round((1 - this.value) * -60));
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
        Font textRenderer = client.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        blit(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

        if (parent.getMicrophoneValue() > 0.95D) {
            RenderSystem.setShaderColor(1.0F, 0.0F, 0.0F, this.alpha);
        } else if (parent.getMicrophoneValue() > 0.7D) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 0.0F, this.alpha);
        } else {
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, this.alpha);
        }
        blit(matrices, this.x + 1, this.y + 1, 1, 47, (int) ((this.width - 2) * parent.getMicrophoneValue()), this.height - 2);

        if (slider) {
            this.renderBg(matrices, client, mouseX, mouseY);
            int j = this.active ? 16777215 : 10526880;
            drawCenteredString(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        //            drawCenteredString(matrices, textRenderer, new TextComponent(String.format("%.2f dB", parent.getHighestDB())),
        //            this.x + this.width / 2, this.y + (this.height - 8) / 2, 16777215);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        for (BackgroundImageButton button : this.microphoneTest) {
            button.x = this.x + this.width + 2;
            button.y = this.y;

            button.render(matrices, mouseX, mouseY, delta);

            if (!VoiceClient.recorder.isAvailable() && button.isHovered(false)) {
                parent.setTooltip(ImmutableList.of(
                        new TranslatableComponent("gui.plasmo_voice.general.not_available")
                ));
            }
        }
    }
}
