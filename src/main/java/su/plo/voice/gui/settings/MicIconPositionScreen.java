package su.plo.voice.gui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.MicrophoneIconPosition;
import su.plo.voice.Voice;

public class MicIconPositionScreen extends Screen {
    private final Screen parent;

    public MicIconPositionScreen(Screen parent) {
        super(new TranslationTextComponent("gui.plasmo_voice.select_mic"));
        this.parent = parent;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        TextComponent text = (new TranslationTextComponent("gui.plasmo_voice.mic_icon_pos_choose"));
        minecraft.font.draw(matrices,
                text,
                (float)(this.width / 2 - minecraft.font.width(text) / 2),
                (float) this.height / 2 - minecraft.font.lineHeight,
                16777215);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.setScreen(parent);
    }

    @Override
    protected void init() {
        super.init();

        int offset = 25;
        int buttonWidth = 100;

        addButton(new Button(offset, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_LEFT.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.TOP_LEFT;
            minecraft.setScreen(parent);
        }));

        addButton(new Button((this.width / 2) - (buttonWidth / 2), offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_CENTER.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.TOP_CENTER;
            minecraft.setScreen(parent);
        }));

        addButton(new Button(this.width - offset - buttonWidth, offset, buttonWidth, 20,
                MicrophoneIconPosition.TOP_RIGHT.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.TOP_RIGHT;
            minecraft.setScreen(parent);
        }));


        addButton(new Button(offset, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_LEFT.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.BOTTOM_LEFT;
            minecraft.setScreen(parent);
        }));

        addButton(new Button((this.width / 2) - (buttonWidth / 2), this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_CENTER.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.BOTTOM_CENTER;
            minecraft.setScreen(parent);
        }));

        addButton(new Button(this.width - offset - buttonWidth, this.height - 20 - offset, buttonWidth, 20,
                MicrophoneIconPosition.BOTTOM_RIGHT.translate(), (button) -> {
            Voice.config.micIconPosition = MicrophoneIconPosition.BOTTOM_RIGHT;
            minecraft.setScreen(parent);
        }));
    }
}
