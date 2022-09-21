package su.plo.voice.client.gui.settings;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.screen.GuiScreen;
import su.plo.lib.client.gui.screen.TooltipScreen;
import su.plo.lib.client.gui.widget.GuiWidgetListener;
import su.plo.lib.client.locale.MinecraftLanguage;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.chat.TextStyle;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.settings.tab.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

// todo: narratables
public final class VoiceSettingsScreen extends GuiScreen implements GuiWidgetListener, TooltipScreen {

    private final PlasmoVoiceClient voiceClient;
    private final ClientConfig config;
    private final TextComponent title;
    @Getter
    private final VoiceSettingsNavigation navigation;
    private final VoiceSettingsAboutFeature aboutFeature;
    private final MicrophoneTestController testController;

    @Getter
    private int titleWidth;
    @Setter
    private List<TextComponent> tooltip;

    public VoiceSettingsScreen(@NotNull MinecraftClientLib minecraft,
                               @NotNull PlasmoVoiceClient voiceClient,
                               @NotNull ClientConfig config,
                               int tab,
                               @NotNull Consumer<Integer> onTabChange) {
        super(minecraft);

        this.voiceClient = voiceClient;
        this.config = config;
        this.title = getSettingsTitle();
        this.navigation = new VoiceSettingsNavigation(minecraft,
                voiceClient,
                this,
                config,
                tab,
                onTabChange
        );
        this.aboutFeature = new VoiceSettingsAboutFeature(minecraft, this);
        this.testController = new MicrophoneTestController(voiceClient, config);
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
                TextComponent.translatable("gui.plasmovoice.devices"),
                new DevicesTabWidget(minecraft, this, voiceClient, config, testController)
        );
        navigation.addTab(
                TextComponent.translatable("gui.plasmovoice.volumes"),
                new VolumesTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                TextComponent.translatable("gui.plasmovoice.activation"),
                new ActivationTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                TextComponent.translatable("gui.plasmovoice.advanced"),
                new AdvancedTabWidget(minecraft, this, voiceClient, config)
        );
        navigation.addTab(
                TextComponent.translatable("gui.plasmovoice.hotkeys"),
                new HotKeysTabWidget(minecraft, this, voiceClient, config)
        );

        addWidget(navigation);

        navigation.init();
    }

    @Override
    public void removed() {
        navigation.removed();
        testController.stop();
    }

    @Override
    public void clearWidgets() {
        navigation.clearTabs();
        super.clearWidgets();
    }

    @Override
    public @NotNull TextComponent getTitle() {
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

    private TextComponent getSettingsTitle() {
        String[] versionSplit = voiceClient.getVersion().split("-");

        String version = versionSplit[0]; // remove -* from version
        TextStyle versionColor = TextStyle.WHITE;
        if (versionSplit.length > 1) {
            if (Objects.equals(versionSplit[1], "ALPHA")) {
                versionColor = TextStyle.YELLOW;
            } else {
                versionColor = TextStyle.RED;
            }
        }

        TextComponent title = TextComponent.translatable(
                "gui.plasmovoice.title",
                TextComponent.literal("Plasmo Voice"),
                TextComponent.literal(version).withStyle(versionColor)
        );
        MinecraftLanguage language = minecraft.getLanguage();

        if (language.getOrDefault("gui.plasmovoice.title").split("%s").length != 3) {
            return TextComponent.literal(String.format("Plasmo Voice %s%s Settings", versionColor, version));
        }

        return title;
    }

    private List<TextComponent> getVersionTooltip() {
        String[] versionSplit = voiceClient.getVersion().split("-");

        if (versionSplit.length < 2) return null;

        if (Objects.equals(versionSplit[1].toLowerCase(), "alpha")) {
            return ImmutableList.of(TextComponent.literal("Plasmo Voice Alpha Branch"));
        } else {
            return ImmutableList.of(TextComponent.literal("Plasmo Voice Dev Branch"));
        }
    }
}
