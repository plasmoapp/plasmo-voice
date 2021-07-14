package su.plo.voice.gui.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import su.plo.voice.client.MicrophoneIconPosition;
import su.plo.voice.client.VoiceClient;

public class MicIconPositionScreen extends Screen {
    private final Screen parent;

    public MicIconPositionScreen(Screen parent) {
        super(new TranslatableText("gui.plasmo_voice.select_mic"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Text text = (new TranslatableText("gui.plasmo_voice.mic_icon_pos_choose"));
        this.client.textRenderer.draw(matrices,
                text,
                (float)(this.width / 2 - this.client.textRenderer.getWidth(text) / 2),
                (float) this.height / 2 - this.client.textRenderer.fontHeight,
                16777215);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.client.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        int offset = 25;
        int buttonWidth = 100;

        addDrawableChild(new ButtonWidget(offset, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_LEFT.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.TOP_LEFT);
            this.client.setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget((this.width / 2) - (buttonWidth / 2), offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_CENTER.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.TOP_CENTER);
            this.client.setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget(this.width - offset - buttonWidth, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_RIGHT.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.TOP_RIGHT);
            this.client.setScreen(parent);
        }));


        addDrawableChild(new ButtonWidget(offset, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_LEFT.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.BOTTOM_LEFT);
            this.client.setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget((this.width / 2) - (buttonWidth / 2), this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_CENTER.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.BOTTOM_CENTER);
            this.client.setScreen(parent);
        }));

        addDrawableChild(new ButtonWidget(this.width - offset - buttonWidth, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_RIGHT.translate(), (button) -> {
            VoiceClient.getClientConfig().setMicIconPosition(MicrophoneIconPosition.BOTTOM_RIGHT);
            this.client.setScreen(parent);
        }));
    }
}
