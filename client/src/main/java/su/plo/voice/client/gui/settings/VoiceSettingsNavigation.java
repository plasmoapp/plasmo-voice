package su.plo.voice.client.gui.settings;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.ResourceLocationUtil;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.lib.mod.client.render.pipeline.RenderPipelines;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.tab.AboutTabWidget;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;
import su.plo.voice.client.gui.settings.tab.TabWidget;
import su.plo.voice.client.gui.settings.widget.TabButton;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

//#if MC>=12005
//$$ import net.minecraft.client.Minecraft;
//$$ import static su.plo.lib.mod.client.gui.widget.GuiWidget.MENU_LIST_BACKGROUND_LOCATION;
//$$ import static su.plo.lib.mod.client.gui.widget.GuiWidget.INWORLD_MENU_LIST_BACKGROUND_LOCATION;
//$$ import static su.plo.lib.mod.client.gui.widget.GuiWidget.FOOTER_SEPARATOR_LOCATION;
//$$ import static su.plo.lib.mod.client.gui.widget.GuiWidget.INWORLD_FOOTER_SEPARATOR_LOCATION;
//#else
import static su.plo.lib.mod.client.gui.widget.GuiWidget.BACKGROUND_LOCATION;
//#endif

public final class VoiceSettingsNavigation implements GuiWidgetListener {

    private static final ResourceLocation MICROPHONE_ICON = ResourceLocationUtil.mod("textures/icons/microphone_menu.png");
    private static final ResourceLocation MICROPHONE_DISABLED_ICON = ResourceLocationUtil.mod("textures/icons/microphone_menu_disabled.png");
    private static final ResourceLocation SPEAKER_ICON = ResourceLocationUtil.mod("textures/icons/speaker_menu.png");
    private static final ResourceLocation SPEAKER_DISABLED_ICON = ResourceLocationUtil.mod("textures/icons/speaker_menu_disabled.png");

    private final PlasmoVoiceClient voiceClient;
    private final VoiceSettingsScreen parent;
    private final VoiceClientConfig config;

    private IconButton toggleMicrophoneButton;
    private IconButton toggleVoiceButton;
    private final List<TabWidget> tabWidgets = Lists.newArrayList();
    private final List<Button> tabButtons = Lists.newArrayList();
    @Nullable
    private AboutTabWidget aboutTabWidget;

    @Getter
    private int active;

    public VoiceSettingsNavigation(@NotNull PlasmoVoiceClient voiceClient,
                                   @NotNull VoiceSettingsScreen parent,
                                   @NotNull VoiceClientConfig config) {
        this.voiceClient = voiceClient;
        this.parent = parent;
        this.config = config;
    }

    // GuiWidgetEventListener impl
    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (keyCode == 256 // GLFW_KEY_ESCAPE
                || keyCode == 258) { // GLFW_KEY_TAB
            Optional<TabWidget> tab = getActiveTab();
            if (!tab.isPresent()) return false;

            if (!(tab.get() instanceof AbstractHotKeysTabWidget)) return false;

            return tab.get().keyPressed(keyCode, modifiers);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        getActiveTab().ifPresent(tab -> {
            if (!(tab instanceof AbstractHotKeysTabWidget)) return;
            tab.mouseReleased(mouseX, mouseY, button);
        });

        return false;
    }

    // Class methods
    public void tick() {
        getActiveTab().ifPresent(TabWidget::tick);
    }

    public void init() {
        this.aboutTabWidget = new AboutTabWidget(parent, voiceClient, config);

        this.toggleMicrophoneButton = new IconButton(
                parent.getWidth() - 52,
                8,
                20,
                20,
                (button) -> config.getVoice().getMicrophoneDisabled().invert(),
                (button, mouseX, mouseY) -> {
                    boolean isMicrophoneDisabled = config.getVoice().getMicrophoneDisabled().value();

                    McTextComponent stateText = isMicrophoneDisabled
                            ? McTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(McTextStyle.RED)
                            : McTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(McTextStyle.GREEN);

                    parent.setTooltip(
                            McTextComponent.translatable(
                                    "gui.plasmovoice.toggle.microphone",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.currently", stateText)
                                            .withStyle(McTextStyle.GRAY)
                            ),
                            mouseX,
                            mouseY
                    );
                },
                () -> config.getVoice().getMicrophoneDisabled().value() ?  MICROPHONE_DISABLED_ICON : MICROPHONE_ICON,
                true
        );

        this.toggleVoiceButton = new IconButton(
                parent.getWidth() - 28,
                8,
                20,
                20,
                (button) -> {
                    boolean isVoiceDisabled = config.getVoice().getDisabled().value();

                    config.getVoice().getDisabled().invert();

                    if (isVoiceDisabled) {
                        voiceClient.getSourceManager().clear();
                    }
                },
                (button, mouseX, mouseY) -> {
                    boolean isVoiceDisabled = config.getVoice().getDisabled().value();

                    McTextComponent stateText = isVoiceDisabled
                            ? McTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(McTextStyle.RED)
                            : McTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(McTextStyle.GREEN);

                    parent.setTooltip(
                            McTextComponent.translatable(
                                    "gui.plasmovoice.toggle.voice",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.currently", stateText)
                                            .withStyle(McTextStyle.GRAY)
                            ),
                            mouseX,
                            mouseY
                    );
                },
                () -> config.getVoice().getDisabled().value() ? SPEAKER_DISABLED_ICON : SPEAKER_ICON,
                true
        );

        parent.addWidget(toggleMicrophoneButton);
        parent.addWidget(toggleVoiceButton);

        getActiveTab().ifPresent((widget) -> {
            widget.init();
            parent.addWidget(widget);
        });
    }

    public void removed() {
        if (aboutTabWidget != null) aboutTabWidget.removed();
        tabWidgets.forEach(TabWidget::removed);
    }

    public void renderButtons(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        int buttonX = 14;
        int buttonY = 36;

        if (!isMinimized()) {
            int buttonsWidth = getButtonsWidth();

            buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);
            buttonY = 8;
        }

        for (int index = 0; index < tabButtons.size(); index++) {
            Button button = tabButtons.get(index);
            button.setActive(active == -1 || index != active);

            if (buttonX + button.getWidth() > parent.getWidth() - 8) {
                buttonX = 14;
                buttonY += 26;
            }

            button.setX(buttonX);
            buttonX += button.getWidth() + 4;
            button.setY(buttonY);

            button.render(context, mouseX, mouseY, delta);
        }

        toggleMicrophoneButton.render(context, mouseX, mouseY, delta);
        toggleVoiceButton.render(context, mouseX, mouseY, delta);
    }

    public void renderTab(@NotNull GuiRenderContext context, int mouseX, int mouseY, float delta) {
        getActiveTab().ifPresent(tab -> tab.render(context, mouseX, mouseY, delta));
    }

    public void renderBackground(@NotNull GuiRenderContext context) {
        int width = parent.getWidth();
        int height = getHeight();

        //#if MC>=12005
        //$$ context.blit(
        //$$         Minecraft.getInstance().level == null ? MENU_LIST_BACKGROUND_LOCATION : INWORLD_MENU_LIST_BACKGROUND_LOCATION,
        //$$         0,
        //$$         0,
        //$$         0.0F,
        //$$         0.0F,
        //$$         width,
        //$$         height,
        //$$         32,
        //$$         32
        //$$ );
        //$$
        //$$ context.blit(
        //$$         Minecraft.getInstance().level == null ? FOOTER_SEPARATOR_LOCATION : INWORLD_FOOTER_SEPARATOR_LOCATION,
        //$$         0,
        //$$         height,
        //$$         0.0F,
        //$$         0.0F,
        //$$         width,
        //$$         2,
        //$$         32,
        //$$         2,
        //$$         RenderPipelines.GUI_TEXTURE_OVERLAY
        //$$ );
        //#else
        context.blitColor(
                BACKGROUND_LOCATION,
                0,
                0,
                0.0F,
                0.0F,
                width,
                height,
                16,
                16,
                new Color(64, 64, 64)
        );

        context.fillGradient(
                width, height + 4, 0, height,
                0, 0, 0, 0,
                0, 0, 0, 255,
                0,
                RenderPipelines.GUI_COLOR_OVERLAY
        );
        //#endif
    }

    public void openTab(int index) {
        parent.getTestController().stop();

        getActiveTab().ifPresent((widget) -> {
            widget.removed();
            parent.removeWidget(widget);
        });

        active = index;

        getActiveTab().ifPresent((widget) -> {
            widget.init();
            parent.addWidget(widget);
        });
    }

    public void addTab(
            @NotNull McTextComponent name,
            @NotNull ResourceLocation iconLocation,
            @NotNull TabWidget tabWidget
    ) {
        int elementIndex = tabWidgets.size();
        Button tabButton = new TabButton(
                0,
                0,
                RenderUtil.getTextWidth(name) + 24,
                20,
                name,
                iconLocation,
                (btn) -> openTab(elementIndex),
                Button.NO_TOOLTIP,
                true
        );

        parent.addWidget(tabButton);

        voiceClient.getEventBus().register(voiceClient, tabWidget);
        tabWidgets.add(tabWidget);
        tabButtons.add(tabButton);
    }

    public void clearTabs() {
        tabWidgets.forEach(tabWidget -> voiceClient.getEventBus().unregister(voiceClient, tabWidget));
        tabWidgets.clear();
        tabButtons.clear();
    }

    public Optional<TabWidget> getActiveTab() {
        return active < 0
                ? Optional.ofNullable(aboutTabWidget)
                : tabWidgets.size() > 0
                ? Optional.of(tabWidgets.get(active))
                : Optional.empty();
    }

    public int getHeight() {
        if (isMinimized()) {
            return 36 + (getLines() * 28);
        } else {
            return 36;
        }
    }

    public boolean isMinimized() {
        int titleWidth = 14 + RenderUtil.getTextWidth(parent.getTitle()) + 4;

        int buttonsWidth = getButtonsWidth();
        int buttonX = (parent.getWidth() / 2) - (buttonsWidth / 2);

        if (buttonX < titleWidth) return true;

        int disableButtonsWidth = 14 + 48; // 48 = 20 + 4 + 20 + 4

        return (titleWidth + buttonsWidth + disableButtonsWidth) > parent.getWidth();
    }

    public int getLines() {
        int buttonX = 14;
        int lines = 1;

        for (Button button : tabButtons) {
            if (buttonX + button.getWidth() > parent.getWidth() - 8) {
                buttonX = 14;
                lines++;
            }

            buttonX += button.getWidth() + 4;
        }

        return lines;
    }

    public int getButtonsWidth() {
        int width = tabButtons.stream()
                .map(Button::getWidth)
                .reduce(0, Integer::sum);
        width += (tabButtons.size() - 1) * 4;

        return width;
    }
}
