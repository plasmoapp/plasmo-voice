package su.plo.voice.gui.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import su.plo.voice.gui.BackgroundScreen;

public class VoiceNotAvailableScreen extends BackgroundScreen {
    public VoiceNotAvailableScreen(TextComponent title, Minecraft client) {
        super(title, 248, client.font.lineHeight + 50, VoiceSettingsScreen.TEXTURE, false);
        this.minecraft = client;
    }

    @Override
    protected void init() {
        super.init();

        addButton(new Button(guiLeft + 10, guiTop + minecraft.font.lineHeight + 20, xSize - 20, 20, new TranslationTextComponent("gui.plasmo_voice.close"), button -> {
            minecraft.setScreen(null);
        }));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        ITextComponent text = this.title;
        this.minecraft.font.draw(matrices, text, (float)(this.width / 2 - this.minecraft.font.width(text) / 2), (float) this.guiTop + 10, 4210752);
    }
}
