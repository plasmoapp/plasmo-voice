package su.plo.voice.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.MicrophoneIconPosition;

public class MicIconPositionScreen extends Screen {
    private final Screen parent;

    public MicIconPositionScreen(Screen parent) {
        super(TextComponent.EMPTY);
        this.parent = parent;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Component text = (new TranslatableComponent("gui.plasmo_voice.general.icons.position.choose"));
        this.minecraft.font.draw(matrices,
                text,
                (float)(this.width / 2 - this.minecraft.font.width(text) / 2),
                (float) this.height / 2 - this.minecraft.font.lineHeight,
                16777215);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        int offset = 25;
        int buttonWidth = 100;

        addWidget(new Button(offset, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_LEFT.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.TOP_LEFT);
            this.minecraft.setScreen(parent);
        }));

        addWidget(new Button((this.width / 2) - (buttonWidth / 2), offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_CENTER.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.TOP_CENTER);
            this.minecraft.setScreen(parent);
        }));

        addWidget(new Button(this.width - offset - buttonWidth, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_RIGHT.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.TOP_RIGHT);
            this.minecraft.setScreen(parent);
        }));


        addWidget(new Button(offset, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_LEFT.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.BOTTOM_LEFT);
            this.minecraft.setScreen(parent);
        }));

        addWidget(new Button((this.width / 2) - (buttonWidth / 2), this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_CENTER.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.BOTTOM_CENTER);
            this.minecraft.setScreen(parent);
        }));

        addWidget(new Button(this.width - offset - buttonWidth, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_RIGHT.translate(), (button) -> {
            VoiceClient.getClientConfig().micIconPosition.set(MicrophoneIconPosition.BOTTOM_RIGHT);
            this.minecraft.setScreen(parent);
        }));
    }
}
