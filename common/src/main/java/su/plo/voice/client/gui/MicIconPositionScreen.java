package su.plo.voice.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.MicrophoneIconPosition;

public class MicIconPositionScreen extends Screen {

    private static final int OFFSET = 25;
    private static final int BUTTON_WIDTH = 100;

    private final Screen parent;

    public MicIconPositionScreen(Screen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        Component text = (Component.translatable("gui.plasmo_voice.general.icons.position.choose"));
        this.minecraft.font.draw(matrices,
                text,
                (float) (this.width / 2 - this.minecraft.font.width(text) / 2),
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

        addRenderableWidget(createButton(OFFSET, OFFSET, MicrophoneIconPosition.TOP_LEFT));
        addRenderableWidget(createButton((this.width / 2) - (BUTTON_WIDTH / 2), OFFSET, MicrophoneIconPosition.TOP_CENTER));
        addRenderableWidget(createButton(this.width - OFFSET - BUTTON_WIDTH, OFFSET, MicrophoneIconPosition.TOP_RIGHT));
        addRenderableWidget(createButton(OFFSET, (this.height / 2) - (BUTTON_WIDTH / 2), MicrophoneIconPosition.BOTTOM_LEFT));
        addRenderableWidget(createButton((this.width / 2) - (BUTTON_WIDTH / 2), this.height - 20 - OFFSET, MicrophoneIconPosition.BOTTOM_CENTER));
        addRenderableWidget(createButton(this.width - OFFSET - BUTTON_WIDTH, this.height - 20 - OFFSET, MicrophoneIconPosition.BOTTOM_RIGHT));
    }

    private Button createButton(int x, int y, MicrophoneIconPosition position) {
        return Button.builder(position.translate(), (button) -> {
                    VoiceClient.getClientConfig().micIconPosition.set(position);
                    this.minecraft.setScreen(parent);
                })
                .pos(x, y)
                .width(BUTTON_WIDTH)
                .build();
    }
}
