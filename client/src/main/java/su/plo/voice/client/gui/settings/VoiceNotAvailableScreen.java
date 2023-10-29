package su.plo.voice.client.gui.settings;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.universal.UDesktop;
import su.plo.voice.universal.UGraphics;
import su.plo.voice.universal.UMatrixStack;

import java.net.URI;

public final class VoiceNotAvailableScreen extends GuiScreen {

    private static final String WIKI_LINK = "https://plasmovoice.com/docs/server/installing";
    private static final int WIDTH = 248;
    private static final int HEIGHT = 50;

    private final BaseVoiceClient voiceClient;

    private Button button;
    private int x;
    private int y;

    private McTextComponent message;

    public VoiceNotAvailableScreen(@NotNull BaseVoiceClient voiceClient) {
        setNotAvailable();
        this.voiceClient = voiceClient;
        voiceClient.getEventBus().register(voiceClient, this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        String formattedMessage = RenderUtil.getFormattedString(message);
        String[] messageLines = formattedMessage.split("\n");

        if (messageLines.length == 3 && button == 0) {
            int lineWidth = UGraphics.getStringWidth(messageLines[2]);
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
                McTextComponent.translatable("message.plasmovoice.close"),
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
        screen.renderBackground(stack);

        int messageLines = RenderUtil.drawStringMultiLineCentered(
                stack,
                message,
                getWidth(),
                y,
                0,
                16777215
        );

        button.setY(y + (UGraphics.getFontHeight() * messageLines) + 20);

        super.render(stack, mouseX, mouseY, delta);
    }

    public void setNotAvailable() {
        this.message = McTextComponent.translatable("gui.plasmovoice.not_available");
    }

    public void setConnecting() {
        this.message = McTextComponent.translatable("gui.plasmovoice.connecting");
    }

    public void setCannotConnect() {
        this.message = McTextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp", WIKI_LINK);
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
        VoiceScreens.INSTANCE.openSettings(voiceClient);
    }

    @EventSubscribe
    public void onTimedOut(@NotNull UdpClientTimedOutEvent event) {
        if (event.isTimedOut()) {
            setConnecting();
        } else {
            VoiceScreens.INSTANCE.openNotAvailable(voiceClient);
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
