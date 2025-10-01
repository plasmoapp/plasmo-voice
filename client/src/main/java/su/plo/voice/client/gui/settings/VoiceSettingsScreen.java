package su.plo.voice.client.gui.settings;

import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
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
import su.plo.voice.client.ModVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.tab.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static su.plo.voice.client.extension.TextKt.getStringSplitToWidth;

//#if MC<12105
import com.mojang.blaze3d.systems.RenderSystem;
//#endif

//#if MC>=12109
//$$ import net.minecraft.client.input.KeyEvent;
//#endif

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

    private final List<Runnable> deferredRenders = new ArrayList<>();

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
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/devices.png"),
                new DevicesTabWidget(this, voiceClient, config, testController)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.volume"),
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/volume.png"),
                new VolumeTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.activation"),
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/activation.png"),
                new ActivationTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.overlay"),
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/overlay.png"),
                new OverlayTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.advanced"),
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/advanced.png"),
                new AdvancedTabWidget(this, voiceClient, config)
        );
        navigation.addTab(
                McTextComponent.translatable("gui.plasmovoice.hotkeys"),
                ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/hotkeys.png"),
                new HotKeysTabWidget(this, voiceClient, config)
        );
        if (voiceClient.getAddonConfigs().size() > 0) {
            navigation.addTab(
                    McTextComponent.translatable("gui.plasmovoice.addons"),
                    ResourceLocation.tryParse("plasmovoice:textures/icons/tabs/addons.png"),
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
    public void render(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        this.tooltip = null;

        //#if MC<12105
        RenderSystem.defaultBlendFunc();
        //#endif

        // background is rendered by default and there is no way override this behavior without mixins
        //#if MC<12106
        screen.renderBackground(context);
        //#endif

        navigation.renderTab(context, mouseX, mouseY, delta);
        navigation.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        // render title
        context.drawString(title, 14, 15, Colors.WHITE);

        navigation.renderButtons(context, mouseX, mouseY, delta);
        aboutFeature.render(context, delta);

        context.flush();
        deferredRenders.forEach(Runnable::run);
        deferredRenders.clear();

        if (tooltip == null && isTitleHovered(mouseX, mouseY))
            tooltip = getVersionTooltip();

        if (tooltip != null) {
            screen.renderTooltipWrapped(
                    context,
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
    public boolean keyPressed(int keyCode, int modifiers) {
        if (super.keyPressed(keyCode, modifiers)) {
            return true;
        }

        boolean hasFocusedWidget = navigation.getActiveTab()
                .map(TabWidget::getFocusedWidget)
                .isPresent();
        if (hasFocusedWidget) {
            return false;
        }

        //#if MC>=12109
        //$$ if (ModVoiceClient.MENU_KEY.matches(new KeyEvent(keyCode, 0, modifiers))) {
        //#else
        if (ModVoiceClient.MENU_KEY.matches(keyCode, 0)) {
        //#endif
            VoiceScreens.INSTANCE.openSettings(voiceClient);
            return true;
        }

        return false;
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
                mouseY >= 15 && mouseY <= (15 + RenderUtil.getFontHeight());
    }

    @EventSubscribe
    public void onTimedOut(@NotNull UdpClientTimedOutEvent event) {
        if (event.isTimedOut()) VoiceScreens.INSTANCE.openNotAvailable(voiceClient);
    }

    @EventSubscribe
    public void onClosed(@NotNull UdpClientClosedEvent event) {
        VoiceScreens.INSTANCE.openNotAvailable(voiceClient);
    }

    public void deferredRender(@NotNull Runnable render) {
        deferredRenders.add(render);
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
