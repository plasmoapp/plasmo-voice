package su.plo.voice.client.gui.settings;

import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.TooltipScreen;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.tab.*;

import java.util.stream.Collectors;

import static su.plo.voice.client.utils.TextKt.getStringSplitToWidth;

// todo: narratables
public final class VoiceSettingsScreen extends GuiScreen implements GuiWidgetListener, TooltipScreen {

    private final BaseVoiceClient voiceClient;
    private final ClientConfig config;
    private final MinecraftTextComponent title;
    @Getter
    private final VoiceSettingsNavigation navigation;
    private final VoiceSettingsAboutFeature aboutFeature;
    @Getter
    private final MicrophoneTestController testController;

    @Getter
    private int titleWidth;
    @Setter
    private @Nullable MinecraftTextComponent tooltip;

    @Setter
    private boolean preventEscClose;

    public VoiceSettingsScreen(@NotNull BaseVoiceClient voiceClient,
                               @NotNull ClientConfig config) {
        this.voiceClient = voiceClient;
        this.config = config;
        this.title = getSettingsTitle();
        this.navigation = new VoiceSettingsNavigation(
                voiceClient,
                this,
                config
        );
        this.aboutFeature = new VoiceSettingsAboutFeature(this);
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

        this.titleWidth = RenderUtil.getTextWidth(getTitle());
        clearWidgets();

        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.devices"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/devices.png"),
                new DevicesTabWidget(this, voiceClient, config, testController)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.volume"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/volume.png"),
                new VolumeTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.activation"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/activation.png"),
                new ActivationTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.overlay"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/overlay.png"),
                new OverlayTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.advanced"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/advanced.png"),
                new AdvancedTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                MinecraftTextComponent.translatable("gui.plasmovoice.hotkeys"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/hotkeys.png"),
                new HotKeysTabWidget(this, voiceClient, config)
        );
        if (voiceClient.getAddonConfigs().size() > 0) {
            navigation.addTab(
                    MinecraftTextComponent.translatable("gui.plasmovoice.addons"),
                    new ResourceLocation("plasmovoice:textures/icons/tabs/addons.png"),
                    new AddonsTabWidget(this, voiceClient, config)
            );
        }

        addWidget(navigation);

        navigation.init();
    }

    @Override
    public void removed() {
        navigation.removed();
        testController.stop();

        config.save(true);

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

    @Override
    public boolean shouldCloseOnEsc() {
        if (preventEscClose) {
            this.preventEscClose = false;
            return false;
        }

        return true;
    }

    // GuiWidget impl
    @Override
    public void render(@NotNull UMatrixStack stack, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        screen.renderBackground(stack);
        navigation.renderTab(stack, mouseX, mouseY, delta);
        navigation.renderBackground(stack);
        super.render(stack, mouseX, mouseY, delta);

        // render title
        RenderUtil.drawString(stack, title, 14, 15, 0xFFFFFF);

        navigation.renderButtons(stack, mouseX, mouseY, delta);
        aboutFeature.render(stack, delta);

        if (tooltip == null && isTitleHovered(mouseX, mouseY))
            tooltip = getVersionTooltip();

        if (tooltip != null) {
            screen.renderTooltip(
                    stack,
                    getStringSplitToWidth(
                            RenderUtil.getFormattedString(tooltip),
                            180,
                            true,
                            true
                    ).stream().map(MinecraftTextComponent::literal).collect(Collectors.toList()),
                    mouseX,
                    mouseY
            );
        }
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
                mouseY >= 15 && mouseY <= (15 + UGraphics.getFontHeight());
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

        String version = versionSplit[0];
        MinecraftTextStyle versionColor = MinecraftTextStyle.WHITE;
        if (versionSplit.length > 1) {
            versionColor = MinecraftTextStyle.YELLOW;
        }

        MinecraftTextComponent title = MinecraftTextComponent.translatable(
                "gui.plasmovoice.title",
                MinecraftTextComponent.literal("Plasmo Voice"),
                MinecraftTextComponent.literal(version).withStyle(versionColor)
        );

        if (LanguageUtil.getOrDefault("gui.plasmovoice.title").split("%").length != 3) {
            return MinecraftTextComponent.literal(String.format("Plasmo Voice %s%s Settings", versionColor, version));
        }

        return title;
    }

    private MinecraftTextComponent getVersionTooltip() {
        String[] versionSplit = voiceClient.getVersion().split("\\+");
        if (versionSplit.length < 2) return null;

        return MinecraftTextComponent.literal("build+" + versionSplit[1]);
    }
}
