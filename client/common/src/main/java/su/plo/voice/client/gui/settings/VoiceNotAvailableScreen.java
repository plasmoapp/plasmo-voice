package su.plo.voice.client.gui.settings;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.Button;
import su.plo.lib.api.client.gui.screen.GuiScreen;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;

import java.util.List;

public final class VoiceNotAvailableScreen extends GuiScreen {

    private static final String WIKI_LINK = "https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server";
    private static final int WIDTH = 248;
    private static final int HEIGHT = 50;

    private final BaseVoiceClient voiceClient;

    private Button button;
    private int x;
    private int y;

    private List<TextComponent> message;

    public VoiceNotAvailableScreen(@NotNull MinecraftClientLib minecraft,
                                   @NotNull BaseVoiceClient voiceClient) {
        super(minecraft);

        setNotAvailable();
        this.voiceClient = voiceClient;
        voiceClient.getEventBus().register(voiceClient, this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (message.size() == 3 && button == 0) {
            int lineWidth = minecraft.getFont().width(message.get(2));
            int lineHeight = minecraft.getFont().getLineHeight();
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
                minecraft,
                x + 10,
                0,
                WIDTH - 20,
                20,
                TextComponent.translatable("message.plasmovoice.close"),
                (button) -> minecraft.setScreen(null),
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
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        int maxWidth = 0;
        for (TextComponent line : message) {
            int lineWidth = minecraft.getFont().width(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        button.setY(y + (minecraft.getFont().getLineHeight() * message.size()) + 20);

        screen.renderBackground();

        for (int i = 0; i < message.size(); i++) {
            TextComponent line = message.get(i);
            render.drawString(
                    line,
                    getWidth() / 2 - minecraft.getFont().width(line) / 2,
                    y + (10 * (i + 1)),
                    16777215
            );
        }

        super.render(render, mouseX, mouseY, delta);
    }

    public void setNotAvailable() {
        this.message = ImmutableList.of(
                TextComponent.translatable("gui.plasmovoice.not_available")
        );
    }

    public void setConnecting() {
        this.message = ImmutableList.of(TextComponent.translatable("gui.plasmovoice.connecting"));
    }

    public void setCannotConnect() {
        this.message = ImmutableList.of(
                TextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_1"),
                TextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_2"),
                TextComponent.translatable("gui.plasmovoice.cannot_connect_to_udp_3", WIKI_LINK)
        );
    }

    private void openLink(@NotNull String link) {
        minecraft.getWindow().openLink(link, true, (ok) -> minecraft.setScreen(this));
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
