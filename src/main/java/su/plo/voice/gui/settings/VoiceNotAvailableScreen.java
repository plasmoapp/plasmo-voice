package su.plo.voice.gui.settings;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import su.plo.voice.gui.BackgroundScreen;

public class VoiceNotAvailableScreen extends BackgroundScreen {
    public VoiceNotAvailableScreen(Text title, MinecraftClient client) {
        super(title, 248, client.textRenderer.fontHeight + 50, VoiceSettingsScreen.TEXTURE, false);
    }

    @Override
    protected void init() {
        super.init();

        addDrawableChild(new ButtonWidget(guiLeft + 10, guiTop + client.textRenderer.fontHeight + 20, xSize - 20, 20, new TranslatableText("gui.plasmo_voice.close"), button -> {
            client.openScreen(null);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        Text text = this.title;
        this.client.textRenderer.draw(matrices, text, (float)(this.width / 2 - this.client.textRenderer.getWidth(text) / 2), (float) this.guiTop + 10, 4210752);
    }
}
