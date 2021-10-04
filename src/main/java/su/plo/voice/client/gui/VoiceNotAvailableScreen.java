package su.plo.voice.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;

public class VoiceNotAvailableScreen extends BackgroundScreen {
    public static final String wiki = "https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server";
    private List<Component> message = ImmutableList.of(new TranslatableComponent("gui.plasmo_voice.not_available"));
    private final Button button;

    public VoiceNotAvailableScreen() {
        super(TextComponent.EMPTY, 248, 50, null, true);
        button = new Button(0, 0, 0, 20, new TranslatableComponent("gui.plasmo_voice.close"), button -> {
            minecraft.setScreen(null);
        });
    }

    public void setConnecting() {
        this.message = ImmutableList.of(new TranslatableComponent("gui.plasmo_voice.connecting"));
    }

    public void setCannotConnect() {
        this.message = ImmutableList.of(
                new TranslatableComponent("gui.plasmo_voice.cannot_connect_to_udp_1"),
                new TranslatableComponent("gui.plasmo_voice.cannot_connect_to_udp_2"),
                new TranslatableComponent("gui.plasmo_voice.cannot_connect_to_udp_3", wiki)
        );
    }

    private void openLink(String linkUrl) {
        this.minecraft.setScreen(new ConfirmLinkScreen(ok -> this.confirmLink(ok, linkUrl), linkUrl, true));
    }

    private void confirmLink(boolean ok, String linkUrl) {
        if (ok) {
            Util.getPlatform().openUri(linkUrl);
        }

        this.minecraft.setScreen(this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.message.size() != 3) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 0) {
            int lineWidth = this.minecraft.font.width(message.get(2));
            float x = (float)(this.width / 2 - lineWidth / 2);

            if (mouseX >= x && mouseX <= x + lineWidth &&
                    mouseY >= this.guiTop + 10 + (minecraft.font.lineHeight * 2) &&
                    mouseY <= this.guiTop + 10 + (minecraft.font.lineHeight * 3)) {
                openLink(wiki);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(button);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        int maxWidth = 0;
        for (Component line : message) {
            int lineWidth = this.minecraft.font.width(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        this.setHeight(50 + (minecraft.font.lineHeight * message.size()));

        button.setWidth(xSize - 20);
        button.x = guiLeft + 10;
        button.y = guiTop + (minecraft.font.lineHeight * message.size()) + 20;

        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);

        int y = this.guiTop + 10;
        for (Component line : message) {
            minecraft.font.draw(matrices, line, (float)(this.width / 2 - this.minecraft.font.width(line) / 2), (float) y, 16777215);
            y += minecraft.font.lineHeight;
        }
    }
}
