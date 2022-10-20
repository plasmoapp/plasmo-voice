package su.plo.voice.client.gui.settings;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.screen.GuiScreen;
import su.plo.lib.api.client.gui.screen.TooltipScreen;
import su.plo.lib.api.client.gui.widget.GuiWidgetListener;
import su.plo.lib.api.client.locale.MinecraftLanguage;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.tab.*;

import java.util.List;
import java.util.Objects;

// todo: narratables
public final class VoiceSettingsScreen extends GuiScreen implements GuiWidgetListener, TooltipScreen {

    private final BaseVoiceClient voiceClient;
    private final ClientConfig config;
    private final MinecraftTextComponent title;
    @Getter
    private final VoiceSettingsNavigation navigation;
    private final VoiceSettingsAboutFeature aboutFeature;
    private final MicrophoneTestController testController;

    @Getter
    private int titleWidth;
    @Setter
    private List<MinecraftTextComponent> tooltip;

    public VoiceSettingsScreen(@NotNull MinecraftClientLib minecraft,
                               @NotNull BaseVoiceClient voiceClient,
                               @NotNull ClientConfig config) {
        super(minecraft);

        this.voiceClient = voiceClient;
        this.config = config;
        this.title = getSettingsTitle();
        this.navigation = new VoiceSettingsNavigation(
                minecraft,
                voiceClient,
                this,
                config
        );
        this.aboutFeature = new VoiceSettingsAboutFeature(minecraft, this);
        this.testController = new MicrophoneTestController(voiceClient, config);

        voiceClient.getEventBus().register(voiceClient, this);
    }

    // GuiScreen impl & override
    @Override
    public void tick() {
        navigation.tick();
        aboutFeature.tick();
    }

    @Override
    public void init() {
        voiceClient.getEventBus().unregister(voiceClient, testController);
        voiceClient.getEventBus().register(voiceClient, testController);

        this.titleWidth = font.width(getTitle());
        clearWidgets();

        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices"),
                "plasmovoice:textures/icons/tabs/devices.png",
                new DevicesTabWidget(minecraft, this, voiceClient, config, testController)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.volume"),
                "plasmovoice:textures/icons/tabs/volume.png",
                new VolumeTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation"),
                "plasmovoice:textures/icons/tabs/activation.png",
                new ActivationTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay"),
                "plasmovoice:textures/icons/tabs/overlay.png",
                new OverlayTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced"),
                "plasmovoice:textures/icons/tabs/advanced.png",
                new AdvancedTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.hotkeys"),
                "plasmovoice:textures/icons/tabs/hotkeys.png",
                new HotKeysTabWidget(minecraft, this, voiceClient, config)
        );

        addWidget(navigation);

        navigation.init();
    }

    @Override
    public void removed() {
        navigation.removed();
        testController.stop();

        voiceClient.getEventBus().unregister(voiceClient, this);
        voiceClient.getEventBus().unregister(voiceClient, testController);
    }

    @Override
    public void clearWidgets() {
        navigation.clearTabs();
        super.clearWidgets();
    }

    @Override
    public @NotNull MinecraftTextComponent getTitle() {
        return title;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        screen.renderBackground();
        navigation.renderTab(render, mouseX, mouseY, delta);
        navigation.renderBackground(render);
        super.render(render, mouseX, mouseY, delta);

        // render title
        screen.drawTextShadow(title, 14, 15, 0xFFFFFF);

        navigation.renderButtons(render, mouseX, mouseY, delta);
        aboutFeature.render(render, delta);

        if (tooltip == null && isTitleHovered(mouseX, mouseY))
            this.tooltip = getVersionTooltip();

        if (tooltip != null)
            screen.renderTooltip(tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isTitleHovered(mouseX, mouseY) && navigation.getActive() >= 0) {
            aboutFeature.titleClicked();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    // Class methods
    public boolean isTitleHovered(double mouseX, double mouseY) {
        return mouseX >= 14 && mouseX <= (14 + titleWidth) &&
                mouseY >= 15 && mouseY <= (15 + font.getLineHeight());
    }

    @EventSubscribe
    public void onTimedOut(@NotNull UdpClientTimedOutEvent event) {
        if (event.isTimedOut()) voiceClient.openNotAvailable();
    }

    @EventSubscribe
    public void onClosed(@NotNull UdpClientClosedEvent event) {
        voiceClient.openNotAvailable();
    }

    private MinecraftTextComponent getSettingsTitle() {
        String[] versionSplit = voiceClient.getVersion().split("\\+");
        versionSplit = versionSplit[0].split("-");

        String version = versionSplit[0]; // remove -* and +* from version
        MinecraftTextStyle versionColor = MinecraftTextStyle.WHITE;
        if (versionSplit.length > 1) {
            if (Objects.equals(versionSplit[1], "ALPHA")) {
                versionColor = MinecraftTextStyle.YELLOW;
            } else {
                versionColor = MinecraftTextStyle.RED;
            }
        }

        MinecraftTextComponent title = MinecraftTextComponent.translatable(
                "gui.plasmovoice.title",
                MinecraftTextComponent.literal("Plasmo Voice"),
                MinecraftTextComponent.literal(version).withStyle(versionColor)
        );
        MinecraftLanguage language = minecraft.getLanguage();

        if (language.getOrDefault("gui.plasmovoice.title").split("%s").length != 3) {
            return MinecraftTextComponent.literal(String.format("Plasmo Voice %s%s Settings", versionColor, version));
        }

        return title;
    }

    private List<MinecraftTextComponent> getVersionTooltip() {
        String[] versionSplit = voiceClient.getVersion().split("\\+");
        versionSplit = versionSplit[0].split("-");
        if (versionSplit.length < 2) return null;

        if (Objects.equals(versionSplit[1].toLowerCase(), "alpha")) {
            return ImmutableList.of(MinecraftTextComponent.literal("Plasmo Voice Alpha"));
        } else {
            return ImmutableList.of(MinecraftTextComponent.literal("Plasmo Voice Dev"));
        }
    }
}
