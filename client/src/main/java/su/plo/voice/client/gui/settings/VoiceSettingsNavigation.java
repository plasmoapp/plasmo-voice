package su.plo.voice.client.gui.settings;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.gui.components.IconButton;
import su.plo.lib.mod.client.gui.widget.GuiWidgetListener;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.config.VoiceClientConfig;
import su.plo.voice.client.gui.settings.tab.AboutTabWidget;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;
import su.plo.voice.client.gui.settings.tab.TabWidget;
import su.plo.voice.client.gui.settings.widget.TabButton;

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

    private final PlasmoVoiceClient voiceClient;
    private final VoiceSettingsScreen parent;
    private final VoiceClientConfig config;

    private final List<Button> disableMicrophoneButtons = Lists.newArrayList();
    private final List<Button> disableVoiceButtons = Lists.newArrayList();
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

        disableMicrophoneButtons.clear();
        disableVoiceButtons.clear();

        // mute mic
        IconButton disableMicrophoneHide = new IconButton(
                parent.getWidth() - 52,
                8,
                20,
                20,
                (button) -> {
                    disableMicrophoneButtons.get(0).setVisible(false);
                    disableMicrophoneButtons.get(1).setVisible(true);
                    config.getVoice().getMicrophoneDisabled().set(true);
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(McTextComponent.translatable(
                            "gui.plasmovoice.toggle.microphone",
                            McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(McTextStyle.GREEN)
                            ).withStyle(McTextStyle.GRAY)
                    ));
                },
                new ResourceLocation("plasmovoice:textures/icons/microphone_menu.png"),
                true
        );

        IconButton disableMicrophoneShow = new IconButton(
                parent.getWidth() - 52,
                8,
                20,
                20,
                (button) -> {
                    disableMicrophoneButtons.get(0).setVisible(true);
                    disableMicrophoneButtons.get(1).setVisible(false);
                    config.getVoice().getMicrophoneDisabled().set(false);

                    if (config.getVoice().getDisabled().value()) {
                        this.disableVoiceButtons.get(0).setVisible(true);
                        this.disableVoiceButtons.get(1).setVisible(false);
                        config.getVoice().getDisabled().set(false);
                    }
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(McTextComponent.translatable(
                            "gui.plasmovoice.toggle.microphone",
                            McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(McTextStyle.RED)
                            ).withStyle(McTextStyle.GRAY)
                    ));
                },
                new ResourceLocation("plasmovoice:textures/icons/microphone_menu_disabled.png"),
                true
        );

        disableMicrophoneHide.setVisible(
                !config.getVoice().getMicrophoneDisabled().value()
                        && !config.getVoice().getDisabled().value()
        );
        disableMicrophoneShow.setVisible(
                config.getVoice().getMicrophoneDisabled().value()
                        || config.getVoice().getDisabled().value()
        );

        // mute speaker
        IconButton disableVoiceHide = new IconButton(
                parent.getWidth() - 28,
                8,
                20,
                20,
                (button) -> {
                    disableVoiceButtons.get(0).setVisible(false);
                    disableVoiceButtons.get(1).setVisible(true);
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    voiceClient.getSourceManager().clear();

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).setVisible(false);
                        disableMicrophoneButtons.get(1).setVisible(true);
                    }
                },
                (button, render, mouseX, mouseY) -> {
                    parent.setTooltip(McTextComponent.translatable(
                            "gui.plasmovoice.toggle.voice",
                            McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.enabled").withStyle(McTextStyle.GREEN)
                            ).withStyle(McTextStyle.GRAY)
                    ));
                },
                new ResourceLocation("plasmovoice:textures/icons/speaker_menu.png"),
                true
        );

        IconButton disableVoiceShow = new IconButton(
                parent.getWidth() - 28,
                8,
                20,
                20,
                (button) -> {
                    disableVoiceButtons.get(0).setVisible(true);
                    disableVoiceButtons.get(1).setVisible(false);
                    config.getVoice().getDisabled().set(!config.getVoice().getDisabled().value());

                    if (!config.getVoice().getMicrophoneDisabled().value()) {
                        disableMicrophoneButtons.get(0).setVisible(true);
                        disableMicrophoneButtons.get(1).setVisible(false);
                    }
                },
                (button, matrices, mouseX, mouseY) -> {
                    parent.setTooltip(McTextComponent.translatable(
                            "gui.plasmovoice.toggle.voice",
                            McTextComponent.translatable("gui.plasmovoice.toggle.currently",
                                    McTextComponent.translatable("gui.plasmovoice.toggle.disabled").withStyle(McTextStyle.RED)
                            ).withStyle(McTextStyle.GRAY)
                    ));
                },
                new ResourceLocation("plasmovoice:textures/icons/speaker_menu_disabled.png"),
                true
        );

        disableVoiceHide.setVisible(!config.getVoice().getDisabled().value());
        disableVoiceShow.setVisible(config.getVoice().getDisabled().value());

        disableMicrophoneButtons.add(disableMicrophoneHide);
        disableMicrophoneButtons.add(disableMicrophoneShow);
        parent.addWidget(disableMicrophoneHide);
        parent.addWidget(disableMicrophoneShow);

        disableVoiceButtons.add(disableVoiceHide);
        disableVoiceButtons.add(disableVoiceShow);
        parent.addWidget(disableVoiceHide);
        parent.addWidget(disableVoiceShow);

        getActiveTab().ifPresent((widget) -> {
            widget.init();
            parent.addWidget(widget);
        });
    }

    public void removed() {
        if (aboutTabWidget != null) aboutTabWidget.removed();
        tabWidgets.forEach(TabWidget::removed);
    }

    public void renderButtons(@NotNull PoseStack stack, int mouseX, int mouseY, float delta) {
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

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            button.render(stack, mouseX, mouseY, delta);
        }

        for (Button button : disableMicrophoneButtons) {
            button.render(stack, mouseX, mouseY, delta);
        }

        for (Button button : disableVoiceButtons) {
            button.render(stack, mouseX, mouseY, delta);
        }
    }

    public void renderTab(@NotNull PoseStack stack, int mouseX, int mouseY, float delta) {
        getActiveTab().ifPresent(tab -> tab.render(stack, mouseX, mouseY, delta));
    }

    public void renderBackground(@NotNull PoseStack stack) {
        int width = parent.getWidth();
        int height = getHeight();

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        //#if MC>=12005
        //$$ RenderSystem.enableBlend();
        //$$
        //$$ RenderUtil.bindTexture(0, Minecraft.getInstance().level == null ? MENU_LIST_BACKGROUND_LOCATION : INWORLD_MENU_LIST_BACKGROUND_LOCATION);
        //$$
        //$$ RenderUtil.blit(
        //$$         stack,
        //$$         0, width,
        //$$         0, height,
        //$$         0,
        //$$         0, width / 32F,
        //$$         0, height / 32F
        //$$ );
        //$$
        //$$ RenderUtil.bindTexture(0, Minecraft.getInstance().level == null ? FOOTER_SEPARATOR_LOCATION : INWORLD_FOOTER_SEPARATOR_LOCATION);
        //$$
        //$$ RenderUtil.blit(
        //$$         stack,
        //$$         0, width,
        //$$         height, height + 2,
        //$$         0,
        //$$         0, width / 32F,
        //$$         0, 1F
        //$$ );
        //$$
        //$$ RenderSystem.disableBlend();
        //#else
        RenderUtil.bindTexture(0, BACKGROUND_LOCATION);

        RenderUtil.blitColor(
                stack,
                0, width,
                0, height,
                0,
                0, width / 32F,
                0, height / 32F,
                64, 64, 64, 255
        );


        RenderSystem.depthFunc(515);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                770, // SRC_ALPHA,
                771, // ONE_MINUS_SRC_ALPHA
                0, // ZERO
                1 // ONE
        );
        //#if MC<11700
        //$$ RenderSystem.shadeModel(7425);
        //#endif

        RenderUtil.fillGradient(
                stack,
                width, height + 4, 0, height,
                0, 0, 0, 0,
                0, 0, 0, 255,
                0
        );

//        render.enableTexture();
        RenderUtil.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        //#if MC<11700
        //$$ RenderSystem.shadeModel(7424);
        //#endif
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
