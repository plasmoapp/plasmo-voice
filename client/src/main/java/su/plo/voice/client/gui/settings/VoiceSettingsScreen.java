package su.plo.voice.client.gui.settings;

import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UMatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.screen.GuiScreen;
import su.plo.lib.mod.client.gui.screen.TooltipScreen;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientTimedOutEvent;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.tab.*;

import java.util.stream.Collectors;

import static su.plo.voice.client.extension.TextKt.getStringSplitToWidth;

// todo: narratables
public final class VoiceSettingsScreen extends GuiScreen implements GuiWidgetListener, TooltipScreen {

    private final BaseVoiceClient voiceClient;
    private final VoiceClientConfig config;
    private final McTextComponent title;
    @Getter
    private final VoiceSettingsNavigation navigation;
    private final VoiceSettingsAboutFeature aboutFeature;
    @Getter
    private final MicrophoneTestController testController;

    @Getter
    private int titleWidth;
    @Setter
    private @Nullable McTextComponent tooltip;

    @Setter
    private boolean preventEscClose;

    public VoiceSettingsScreen(@NotNull BaseVoiceClient voiceClient) {
        this.voiceClient = voiceClient;
        this.config = voiceClient.getConfig();
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
                McTextComponent.translatable("gui.plasmovoice.devices"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/devices.png"),
                new DevicesTabWidget(this, voiceClient, config, testController)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.volume"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/volume.png"),
                new VolumeTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.activation"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/activation.png"),
                new ActivationTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.overlay"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/overlay.png"),
                new OverlayTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.advanced"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/advanced.png"),
                new AdvancedTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.hotkeys"),
                new ResourceLocation("plasmovoice:textures/icons/tabs/hotkeys.png"),
                new HotKeysTabWidget(this, voiceClient, config)
        );
        if (voiceClient.getAddonConfigs().size() > 0) {
            navigation.addTab(
                    McTextComponent.translatable("gui.plasmovoice.addons"),
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

        navigation.getActiveTab().ifPresent(TabWidget::removed);
        voiceClient.getEventBus().unregister(voiceClient, this);
        voiceClient.getEventBus().unregister(voiceClient, testController);
    }

    @Override
    public void clearWidgets() {
        navigation.clearTabs();
        super.clearWidgets();
    }

    @Override
    public @NotNull McTextComponent getTitle() {
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
                    ).stream().map(McTextComponent::literal).collect(Collectors.toList()),
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
        if (event.isTimedOut()) VoiceScreens.INSTANCE.openNotAvailable(voiceClient);
    }

    @EventSubscribe
    public void onClosed(@NotNull UdpClientClosedEvent event) {
        VoiceScreens.INSTANCE.openNotAvailable(voiceClient);
    }

    private McTextComponent getSettingsTitle() {
        String[] versionSplit = voiceClient.getVersion().split("\\+");

        String version = versionSplit[0];
        McTextStyle versionColor = McTextStyle.WHITE;
        if (versionSplit.length > 1) {
            versionColor = McTextStyle.YELLOW;
        }

        McTextComponent title = McTextComponent.translatable(
                "gui.plasmovoice.title",
                McTextComponent.literal("Plasmo Voice"),
                McTextComponent.literal(version).withStyle(versionColor)
        );

        if (LanguageUtil.getOrDefault("gui.plasmovoice.title").split("%").length != 3) {
            return McTextComponent.literal(String.format("Plasmo Voice %s%s Settings", versionColor, version));
        }

        return title;
    }

    private McTextComponent getVersionTooltip() {
        String[] versionSplit = voiceClient.getVersion().split("\\+");
        if (versionSplit.length < 2) return null;

        return McTextComponent.literal("build+" + versionSplit[1]);
    }
}
