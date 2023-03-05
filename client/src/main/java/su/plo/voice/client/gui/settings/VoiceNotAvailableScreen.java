package su.plo.voice.client.gui.settings;

import com.google.common.collect.ImmutableList;
import gg.essential.universal.UDesktop;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import gg.essential.universal.UScreen;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;

import java.net.URI;
import java.util.List;

public final class VoiceNotAvailableScreen extends GuiScreen {

    private static final String WIKI_LINK = "https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server";
    private static final int WIDTH = 248;
    private static final int HEIGHT = 50;

    private final BaseVoiceClient voiceClient;

    private Button button;
    private int x;
    private int y;

    private List<MinecraftTextComponent> message;

    public VoiceNotAvailableScreen(@NotNull BaseVoiceClient voiceClient) {
        setNotAvailable();
        this.voiceClient = voiceClient;
        voiceClient.getEventBus().register(voiceClient, this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (message.size() == 3 && button == 0) {
            int lineWidth = RenderUtil.getTextWidth(message.get(2));
            int lineHeight = UGraphics.getFontHeight();
            float x = (float) (getWidth() / 2 - lineWidth / 2);

            if (mouseX >= x && mouseX <= x + lineWidth &&
                    mouseY >= y + 10 + (lineHeight * 2) &&
                    mouseY <= y + 10 + (lineHeight * 3)) {
                openLink(WIKI_LINK);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void init() {
        this.x = (getWidth() - WIDTH) / 2;
        this.y = (getHeight() - HEIGHT) / 2;

        this.button = new Button(
                x + 10,
                0,
                WIDTH - 20,
                20,
                MinecraftTextComponent.translatable("message.plasmovoice.close"),
                (button) -> ScreenWrapper.openScreen(null),
                Button.NO_TOOLTIP
        );
        clearWidgets();
        addRenderWidget(button);
    }

    @Override
    public void removed() {
        voiceClient.getEventBus().unregister(voiceClient, this);
    }

    @Override
    public void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        int maxWidth = 0;
        for (MinecraftTextComponent line : message) {
            int lineWidth = RenderUtil.getTextWidth(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        button.setY(y + (UGraphics.getFontHeight() * message.size()) + 20);

        screen.renderBackground(stack);

        for (int i = 0; i < message.size(); i++) {
            MinecraftTextComponent line = message.get(i);
            RenderUtil.drawString(
                    stack,
                    line,
                    getWidth() / 2 - RenderUtil.getTextWidth(line) / 2,
                    y + (10 * (i + 1)),
                    16777215
            );
        }

        super.render(stack, mouseX, mouseY, delta);
    }

    public void setNotAvailable() {
        this.message = ImmutableList.of(
                MinecraftTextComponent.translatable("gui.plasmovoice.not_available")
        );
    }

    public void setConnecting() {
        this.message = ImmutableList.of(MinecraftTextComponent.translatable("gui.plasmovoice.connecting"));
    }

    public void setCannotConnect() {
        this.message = ImmutableList.of(
                MinecraftTextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_1"),
                MinecraftTextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_2"),
                MinecraftTextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_3", WIKI_LINK)
        );
    }

    private void openLink(@NotNull String link) {
        try {
            UDesktop.browse(new URI(link));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventSubscribe
    public void onConnect(@NotNull UdpClientConnectedEvent event) {
        voiceClient.openSettings();
    }

    @EventSubscribe
    public void onTimedOut(@NotNull UdpClientTimedOutEvent event) {
        if (event.isTimedOut()) {
            setConnecting();
        } else {
            voiceClient.openSettings();
        }
    }

    @EventSubscribe
    public void onClosed(@NotNull UdpClientClosedEvent event) {
        switch (event.getReason()) {
            case TIMED_OUT:
            case FAILED_TO_CONNECT:
                setCannotConnect();
                break;
            case RECONNECT:
                setConnecting();
                break;
            default:
                setNotAvailable();
                break;
        }
    }
}
