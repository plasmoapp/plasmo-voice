package su.plo.voice.gui.settings;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.gui.BackgroundScreen;
import su.plo.voice.gui.DeviceListWidget;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.DataLines;
import su.plo.voice.sound.ThreadSoundQueue;

public class SpeakerSelectScreen extends BackgroundScreen {
    private static final Identifier TEXTURE = new Identifier("plasmo_voice", "textures/gui/settings_device.png");
    private final MinecraftClient client;
    private final Screen parent;
    private DeviceListWidget deviceListWidget;

    public SpeakerSelectScreen(Screen parent) {
        super(new TranslatableText("gui.plasmo_voice.select_speaker"), 248, 180, TEXTURE, true);
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
    }

    protected void init() {
        super.init();
        this.deviceListWidget = new DeviceListWidget(this.client, 240, 132, this.guiTop + 12,
                new TranslatableText("gui.plasmo_voice.select_speaker"));
        this.deviceListWidget.setLeftPos(this.guiLeft + 6);

        for(String device : DataLines.getSpeakerNames()) {
            this.deviceListWidget.children().add(new DeviceListWidget.DeviceEntry(this.client, device, (speaker -> {
                VoiceClient.getClientConfig().setSpeaker(speaker);
                VoiceClient.getClientConfig().save();
                this.client.setScreen(this.parent);

                // close all sound threads
                SocketClientUDPQueue.talking.clear();
                SocketClientUDPQueue.audioChannels.values().forEach(ThreadSoundQueue::closeAndKill);
                SocketClientUDPQueue.audioChannels.clear();
            })));
        }

        addDrawableChild(new ButtonWidget(guiLeft + 7, guiTop + ySize - 30, xSize - 14, 20,
                new TranslatableText("gui.plasmo_voice.back"), button -> {
            this.client.setScreen(parent);
        }));
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        // header
        drawTexture(matrices, guiLeft, guiTop, 0, 0, xSize, headerHeight, 512, 512);

        // main
        for(int y = 10; y < ySize - (headerHeight + footerHeight); y += 180) {
            int h = 180;
            if(h > ySize) {
                h = ySize - (headerHeight + footerHeight);
            } else if(h == ySize) {
                h = h - (headerHeight + footerHeight);
            } else if(y + h > ySize) {
                h = ySize - (headerHeight + footerHeight) - h;
            }
            drawTexture(matrices, guiLeft, guiTop + y, 0, headerHeight, xSize, h, 512, 512);
        }

        this.deviceListWidget.render(matrices, mouseX, mouseY, delta);

        // footer
        drawTexture(matrices, guiLeft, guiTop + ySize - footerHeight, 0, 190, xSize, footerHeight, 512, 512);

        RenderSystem.setShaderTexture(0, VoiceSettingsScreen.TEXTURE);
        drawTexture(matrices, guiLeft, guiTop + ySize - 40, 0, 160, xSize, 40, 512, 512);

        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, guiLeft, guiTop + ySize - 41, 0, 192, xSize, 1, 512, 512);

        super.render(matrices, mouseX, mouseY, delta);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return super.mouseScrolled(mouseX, mouseY, amount)
                || this.deviceListWidget.mouseScrolled(mouseX, mouseY, amount);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button)
                || this.deviceListWidget.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
                || this.deviceListWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}